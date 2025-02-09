package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.DirectControlHandler
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.keybind
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.minecraft.world.entity.Relative
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.util.Vector
import kotlin.math.round
import kotlin.math.roundToInt

class PlayerDirectControlInput(override val controller: PlayerController
) : DirectControlInput, PlayerInput{
	override val player get() = controller.player
	override var selectedSpeed: Int
		get() = player.inventory.heldItemSlot
		set(value) {}
	override var isBoosting : Boolean
		get() = player.isSneaking
		set(value) {}

	override fun create() {
		val message = ofChildren(
			text("Direct Control: ", GRAY),
			text("ON ", GRAY),
			text("[Use /dc to turn it off - scroll or use hotbar keys to adjust speed - use W/A/S/D to maneuver - hold sneak (", YELLOW),
			keybind("key.sneak", YELLOW),
			text(") for a boost]", YELLOW)
		)

		controller.sendMessage(message)

		player.walkSpeed = 0.009f

		val playerLoc = player.location
		val newCenter = playerLoc.toBlockLocation().add(0.5, playerLoc.y.rem(1)+0.001, 0.5)

		starship.directControlCenter = newCenter
		player.teleport(newCenter)
	}

	override fun destroy() {
		controller.sendMessage(ofChildren(text("Direct Control: ", GRAY), text("OFF ", NamedTextColor.RED), text("[Use /dc to turn it on]", YELLOW)))

		player.walkSpeed = 0.2f // default
	}

	override fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {
		val inventory = player.inventory

		val previousSlot = event.previousSlot
		val oldItem = inventory.getItem(previousSlot)

		val newSlot = event.newSlot
		val newItem = player.inventory.getItem(newSlot)

		inventory.setItem(newSlot, oldItem)
		inventory.setItem(previousSlot, newItem)

		val baseSpeed = DirectControlHandler.calculateSpeed(newSlot)
		val cooldown: Long = DirectControlHandler.calculateCooldown(starship.directControlCooldown, newSlot)
		val speed = (10.0f * baseSpeed * starship.directControlSpeedModifierFromIonTurrets *
				starship.directControlSpeedModifierFromHeavyLasers * (1000.0f / cooldown)).roundToInt() / 10.0f

		player.sendActionBar(text("Speed: $speed", NamedTextColor.AQUA))
	}

	override fun getData(): DirectControlInput.DirectControlData {
		// Use the player's location
		val pilotLocation = player.location

		var center = starship.directControlCenter
		if (center == null) {
			center = pilotLocation.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}

		// Calculate the movement vector by getting how far the player has moved from the center vector
		var vector = pilotLocation.toVector().subtract(center.toVector())
		vector.setY(0)
		vector.normalize()

		val direction = starship.getTargetForward()

		// Create a separate location that contains the direct control center and the direction of the starship
		val directionWrapper = center.clone()
		directionWrapper.direction = Vector(direction.modX, direction.modY, direction.modZ)
		// Crate a separate location which contains the player's view direction
		val playerDirectionWrapper = center.clone()
		playerDirectionWrapper.direction = pilotLocation.direction
		// Create a separate location which the player's direction offset
		val vectorWrapper = center.clone()
		vectorWrapper.direction = vector
		// Set the horizontal rotation of the center clone containing the player's delta
		// The new yaw value contains an exaggerated stafe, and cancels out ascending / descending inputs.
		vectorWrapper.yaw = vectorWrapper.yaw - (playerDirectionWrapper.yaw - directionWrapper.yaw)
		vector = vectorWrapper.direction

		vector.x = round(vector.x)
		vector.setY(0)
		vector.z = round(vector.z)

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
				true,
				PlayerTeleportEvent.TeleportCause.PLUGIN
			)
		}
		return DirectControlInput.DirectControlData(vector,selectedSpeed,isBoosting)
	}
}
