package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.nations.utils.getPing
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.CruiseData
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising.startCruising
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising.stopCruising
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.keybind
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.minecraft.world.entity.Relative
import org.bukkit.Material
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max

class PlayerDirectCruiseControlInput(override val controller: PlayerController): PlayerInput, DirectCruiseControlInput{
	override val player get() = controller.player

	private var lastTertiaryInput = 0

	private var internalTick = 0
	var ticksToBlockMovementFor = 0

	override fun create() {
		val message = ofChildren(
			text("Cruise Control: ", GRAY),
			text("ON ", GRAY),
			text("[Use /cc to turn it off - use W/A/S/D to maneuver - hold sneak (", YELLOW),
			keybind("key.sneak", YELLOW),
			text(") to move down - hold jump(", YELLOW),
			keybind("key.jump", YELLOW),
			text(") to move up - hold both to stop cruising]", YELLOW)
		)

		controller.sendMessage(message)

		if (player.getSetting(PlayerSettings::floatWhileDc) == true) {
			player.walkSpeed = 0f
			player.flySpeed = 0f
			player.allowFlight = true
			player.isFlying = true
		} else {
			player.walkSpeed = 0.009f
			player.allowFlight = false
		}

		val playerLoc = player.location
		val newCenter = playerLoc.toBlockLocation().add(0.5, playerLoc.y.rem(1), 0.5)

		starship.directControlCenter = newCenter
		player.teleport(newCenter)
	}

	override fun destroy() {
		controller.sendMessage(ofChildren(text("Cruise Control: ", GRAY), text("OFF ", NamedTextColor.RED), text("[Use /cc to turn it on]", YELLOW)))

		player.walkSpeed = 0.2f // default
		player.flySpeed = 0.06f
		player.isFlying = false
	}

	override fun getData(): CruiseData {
		//if the player is not holding a clock, do not accept anything
		if (player.inventory.itemInMainHand.type != Material.CLOCK) return starship.cruiseData
		val currentInput = player.currentInput

		var center = starship.directControlCenter
		if (center == null) {
			starship.debug("Cruise control center adjusted")
			val pilotLocation = player.location
			center = pilotLocation.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}

		val effectiveCooldown = ceil(starship.manualMoveCooldownMillis/50.0) * 50

		val shiftFlySpeed = max(starship.type.balancing.maxSneakFlyAccel.d(), 1.0).div(effectiveCooldown).times(2000.0).toInt().toDouble()

		var dir = player.eyeLocation.direction.normalize()
		dir.setY(0)
		if(currentInput.isForward){
			starship.directCruiseSpeedAddition = shiftFlySpeed
		}
		else {
			starship.directCruiseSpeedAddition = 0.0
		}

		//Handle forward diagonal, and Left
		if(currentInput.isLeft && currentInput.isForward) dir.rotateAroundY(PI/4)
		else if(currentInput.isBackward && currentInput.isLeft) dir.rotateAroundY(3*PI/4)
		else if(currentInput.isLeft) dir.rotateAroundY(PI/2)

		//handle Forward diagnonal, and right
		else if(currentInput.isRight && currentInput.isForward) dir.rotateAroundY(-PI/4)
		else if(currentInput.isBackward && currentInput.isRight) dir.rotateAroundY(-3*PI/4)
		else if(currentInput.isRight)dir.rotateAroundY(-PI/2)
		else if(currentInput.isBackward) dir.multiply(-1)

		//handle side cruising + vertical element
		val verticalDistance = shiftFlySpeed/starship.cruiseData.targetSpeed
		if(currentInput.isJump) dir.add(Vector(0.0, verticalDistance, 0.0))
		if(currentInput.isSneak) dir.add(Vector(0.0, -verticalDistance, 0.0))

		//handle just vertical element on its own. No side cruising
		if((currentInput.isSneak || currentInput.isJump) && !(currentInput.isForward || currentInput.isRight || currentInput.isBackward || currentInput.isLeft)){
			dir = Vector(0.0,verticalDistance.times(if(currentInput.isSneak) -1.0 else 1.0),0.0)
		}
		//if there is a vertical element, do not let the directCruiseSpeedAddition be added
		if (currentInput.isSneak || currentInput.isJump) starship.directCruiseSpeedAddition = 0.0

		//handle tertiary control
		if(currentInput.isSprint) {
			if(player.server.currentTick-lastTertiaryInput > 10) {
				handleTertiaryInput(starship)
				lastTertiaryInput = player.server.currentTick
			}
		}

		// Ping compensation
		val refreshRate = if (player.getSettingOrThrow(PlayerSettings::dcRefreshRate) == -1) {getPing(player) * 2.0}
		else (player.getSettingOrThrow(PlayerSettings::dcRefreshRate).toDouble())
		val catchCooldown = (ceil(refreshRate / 50.0)).toInt().coerceAtLeast(2)

		internalTick++
		if (internalTick % catchCooldown != 0) return starship.cruiseData // reduce teleports to make it non hyper sensitive
		internalTick = 0

		if (player.getSetting(PlayerSettings::floatWhileDc) == true) {
			player.walkSpeed = 0f
			player.flySpeed = 0f
			player.isFlying = true
		} else {
			player.walkSpeed = 0.009f
		}

		// If player moved, teleport them back to dc center
		if (currentInput.isForward || currentInput.isBackward || currentInput.isLeft || currentInput.isRight || currentInput.isJump || currentInput.isSneak) {
			val newLoc = center.clone()

			player.minecraft.teleportTo(
				newLoc.world.minecraft,
				newLoc.x,
				newLoc.y,
				newLoc.z,
				setOf(
					Relative.X_ROT,
					Relative.Y_ROT,
				),
				0f,
				0f,
				false,
				PlayerTeleportEvent.TeleportCause.PLUGIN
			)
		}

		if(ticksToBlockMovementFor != 0){
			ticksToBlockMovementFor -=1
			return starship.cruiseData
		}

		//If the player gives an input, change our direction
		if(currentInput.isForward || currentInput.isBackward || currentInput.isLeft || currentInput.isRight || currentInput.isJump || currentInput.isSneak) {
			ticksToBlockMovementFor +=2
			startCruising(
				starship.controller, starship, dir, true
			)
		}
		//if the player doesn't provide an input stop the ship moving
		else {
			stopCruising(starship.controller, starship)
		}

		return starship.cruiseData
	}

	override fun handleJump(event: PlayerJumpEvent) {
		event.isCancelled = true
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {}
}
