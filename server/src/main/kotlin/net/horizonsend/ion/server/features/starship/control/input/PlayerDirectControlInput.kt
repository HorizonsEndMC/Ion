package net.horizonsend.ion.server.features.starship.control.input

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.nations.utils.getPing
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.DirectControlHandler
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.keybind
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.minecraft.world.entity.Relative
import org.bukkit.block.BlockFace
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.roundToInt

class PlayerDirectControlInput(override val controller: PlayerController) : DirectControlInput, PlayerInput{
	override val player get() = controller.player
	override val selectedSpeed: Double get() = player.inventory.heldItemSlot.toDouble()

	override val isBoosting : Boolean
		get() {
			return if (player.getSetting(PlayerSettings::toggleDcBoost) == true) boostToggleOverride
			else if (player.getSetting(PlayerSettings::reverseDcBoost) == false) player.isSneaking
			else !player.isSneaking
		}

	private var internalTick = 0
	private var cachedState = DirectControlInput.DirectControlData(Vector(), 8.0, false)
	private var boostToggleOverride = false

	override fun create() {
		val message = ofChildren(
			text("Direct Control: ", GRAY),
			text("ON ", GRAY),
			text("[Use /dc to turn it off - scroll or use hotbar keys to adjust speed - use W/A/S/D to maneuver - hold sneak (", YELLOW),
			keybind("key.sneak", YELLOW),
			text(") for a boost]", YELLOW)
		)

		controller.sendMessage(message)

		if (player.getSetting(PlayerSettings::floatWhileDc) == true) {
			player.walkSpeed = 0f
			player.flySpeed = 0f
			player.allowFlight = true
			player.isFlying = true
		} else {
			player.walkSpeed = 0.009f
		}

		val playerLoc = player.location
		val newCenter = playerLoc.toBlockLocation().add(0.5, playerLoc.y.rem(1), 0.5)

		starship.directControlCenter = newCenter
		player.teleport(newCenter)
	}

	override fun destroy() {
		controller.sendMessage(ofChildren(text("Direct Control: ", GRAY), text("OFF ", NamedTextColor.RED), text("[Use /dc to turn it on]", YELLOW)))

		player.walkSpeed = 0.2f // default
		player.flySpeed = 0.06f
		player.isFlying = false
	}

	override fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {
		val inventory = player.inventory

		val previousSlot = event.previousSlot
		val oldItem = inventory.getItem(previousSlot)

		val newSlot = event.newSlot
		val newItem = player.inventory.getItem(newSlot)

		inventory.setItem(newSlot, oldItem)
		inventory.setItem(previousSlot, newItem)

		val baseSpeed = DirectControlHandler.calculateSpeed(newSlot.toDouble())
		val oversizeModifier = if (starship.initialBlockCount > StarshipType.DESTROYER.maxSize) 0.5 else 1.0
		val cooldown: Long = DirectControlHandler
			.calculateCooldown(starship.directControlCooldown, newSlot.toDouble()).toLong()
		val speed = (10.0f * baseSpeed * starship.directControlSpeedModifierFromIonTurrets *
				starship.directControlSpeedModifierFromHeavyLasers * oversizeModifier * (1000.0f / cooldown)).roundToInt() / 10.0f

		player.sendActionBar(text("Speed: $speed", NamedTextColor.AQUA))
	}

	@Suppress("UnstableApiUsage")
	override fun getData(): DirectControlInput.DirectControlData {

		val input = player.currentInput ?: return cachedState

		var center = starship.directControlCenter
		if (center == null) {
			starship.debug("Direct control center adjusted")
			val pilotLocation = player.location
			center = pilotLocation.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}

		val direction = starship.getTargetForward()

		// Determine movement intent from input keys
		var strafe = 0.0
		var ascend = 0.0

		if (input.isLeft) strafe -= 1.0
		if (input.isRight) strafe += 1.0
		if (input.isForward) ascend += 1.0
		if (input.isBackward) ascend -= 1.0

		// Convert to world-relative vector
		val vector = when (direction) {
			BlockFace.NORTH -> Vector(strafe, 0.0, -ascend)
			BlockFace.SOUTH -> Vector(-strafe, 0.0, ascend)
			BlockFace.WEST ->  Vector(-ascend, 0.0, -strafe)
			BlockFace.EAST ->  Vector(ascend, 0.0, strafe)
			else -> Vector()
		}

		// Normalize and round vector
		if (vector.lengthSquared() > 0.0) vector.normalize()
		vector.x = round(vector.x)
		vector.z = round(vector.z)
		vector.y = 0.0

		// Ping compensation
		val refreshRate = if (player.getSettingOrThrow(PlayerSettings::dcRefreshRate) == -1) {getPing(player) * 2.0}
		else (player.getSettingOrThrow(PlayerSettings::dcRefreshRate).toDouble())
		val catchCooldown = (ceil(refreshRate / 50.0)).toInt().coerceAtLeast(2)

		cachedState = DirectControlInput.DirectControlData(vector, selectedSpeed, isBoosting)

		internalTick++
		if (internalTick % catchCooldown != 0) return cachedState // reduce teleports to make it non hyper sensitive
		internalTick = 0

		if (player.getSetting(PlayerSettings::floatWhileDc) == true) {
			player.walkSpeed = 0f
			player.flySpeed = 0f
			player.isFlying = true
		} else {
			player.walkSpeed = 0.009f
			player.flySpeed = 0.06f
			player.isFlying = false
		}

		// If player moved, teleport them back to dc center
		if (vector.x != 0.0 || vector.z != 0.0) {
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
		starship.debug(cachedState.toString())
		return cachedState
	}

	override fun handleJump(event: PlayerJumpEvent) {
		event.isCancelled = true
	}

	override fun handleSneak(event: PlayerToggleSneakEvent) {
		if (!event.isSneaking) return

		if (player.getSetting(PlayerSettings::toggleDcBoost) == true) {
			boostToggleOverride = !boostToggleOverride
		}
	}
}
