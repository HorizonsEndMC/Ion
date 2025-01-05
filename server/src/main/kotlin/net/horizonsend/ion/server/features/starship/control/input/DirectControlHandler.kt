package net.horizonsend.ion.server.features.starship.control.input

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.utils.getPing
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
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
import java.util.Collections
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sign

class DirectControlHandler(controller: PlayerController) : PlayerMovementInputHandler(controller, "Direct Control") {
	private val directControlPreviousVectors = LinkedBlockingQueue<Vector>(4)
	private val directControlVector = Vector()

	override fun create() {
		val message = ofChildren(
			text("Direct Control: ", GRAY),
			text("ON ", GRAY),
			text("[Use /dc to turn it off - scroll or use hotbar keys to adjust speed - use W/A/S/D to maneuver - hold sneak (", YELLOW),
			keybind("key.sneak", YELLOW),
			text(") for a boost]", YELLOW)
		)

		controller.sendMessage(message)

		val player = controller.player

		player.walkSpeed = 0.009f

		val playerLoc = player.location
		val newCenter = playerLoc.toBlockLocation().add(0.5, playerLoc.y.rem(1)+0.001, 0.5)

		starship.directControlCenter = newCenter
		player.teleport(newCenter)
	}

	override fun destroy(new: PlayerMovementInputHandler) {
		controller.sendMessage(ofChildren(text("Direct Control: ", GRAY), text("OFF ", NamedTextColor.RED), text("[Use /dc to turn it on]", YELLOW)))

		directControlVector.x = 0.0
		directControlVector.y = 0.0
		directControlVector.z = 0.0

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

		val baseSpeed = calculateSpeed(newSlot)
		val cooldown: Long = calculateCooldown(starship.directControlCooldown, newSlot)
		val speed = (10.0f * baseSpeed * (1000.0f / cooldown)).roundToInt() / 10.0f

		player.sendActionBar(text("Speed: $speed", NamedTextColor.AQUA))
	}

	override fun tick() {
		if (starship.isTeleporting) return

		if (starship.type == StarshipType.PLATFORM) return controller.userErrorAction("This ship type is not capable of moving.")

		if (Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) {
			starship.setDirectControlEnabled(false)
			return
		}

		// Ping compensation
		val ping = getPing(player)
		val movementCooldown = starship.directControlCooldown
		val playerDcModifier = PlayerCache[player.uniqueId].dcSpeedModifier
		val speedFac = if (ping > movementCooldown) max(2, playerDcModifier) else playerDcModifier

		val selectedSpeed = controller.selectedDirectControlSpeed

		val cooldown = calculateCooldown(movementCooldown, selectedSpeed) * speedFac
		val currentTime = System.currentTimeMillis()
		val lastManualMove = starship.lastManualMove

		val elapsedSinceLastMove = currentTime - lastManualMove
		if (elapsedSinceLastMove < cooldown) return

		starship.lastManualMove = currentTime

		// Start calculating offset
		var dx = 0
		var dy = 0
		var dz = 0

		// The starship's direction
		val direction = starship.getTargetForward()
		val targetSpeed = (calculateSpeed(selectedSpeed) * starship.directControlSpeedModifierFromIonTurrets *
				starship.directControlSpeedModifierFromHeavyLasers).toInt()

		// Initialize forward movement
		dx += (targetSpeed * direction.modX)
		dz += (targetSpeed * direction.modZ)

		// Boost if shift flying
		if (controller.isSneakFlying()) {
			dx *= 2
			dz *= 2
		}

		// Use the player's location if available, otherwise the computer loc
		val pilotLocation = player.location

		var center = starship.directControlCenter
		// If direct control center has not been set, calculate a new direct control center
		if (center == null) {
			center = pilotLocation.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}

		// Calculate the movement vector by getting how far the player has moved from the center vector
		var playerDeltaVector = pilotLocation.toVector().subtract(center.toVector())
		playerDeltaVector.setY(0)     // Only consider horizontal changes
		playerDeltaVector.normalize() // Normalize

		// Create a separate location that contains the direct control center and the direction of the starship
		val centerWithShipDirection = center.clone()
		centerWithShipDirection.direction = Vector(direction.modX, direction.modY, direction.modZ)

		// Crate a separate location which contains the player's view direction
		val centerWithPlayerDirection = center.clone()
		centerWithPlayerDirection.direction = player.location.direction

		// Create a separate location which the player's direction offset
		val centerWithPlayerDelta = center.clone()
		centerWithPlayerDelta.direction = playerDeltaVector

		// Set the horizontal rotation of the center clone containing the player's delta
		// The new yaw value contains an exaggerated stafe, and cancels out ascending / descending inputs.
		centerWithPlayerDelta.yaw = centerWithPlayerDelta.yaw - (centerWithPlayerDirection.yaw - centerWithShipDirection.yaw)

		// Use the calculated value as the delta vector
		playerDeltaVector = centerWithPlayerDelta.direction

		playerDeltaVector.x = round(playerDeltaVector.x)
		playerDeltaVector.setY(0)
		playerDeltaVector.z = round(playerDeltaVector.z)

		val vectors = directControlPreviousVectors
		if (vectors.size > 3) {
			vectors.poll()
		}

		// Store strafe vectors
		vectors.add(playerDeltaVector)

		// If player moved, teleport them back to dc center
		if (playerDeltaVector.x != 0.0 || playerDeltaVector.z != 0.0) {
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
//
//			newLoc.pitch = player.location.pitch
//			newLoc.yaw = player.location.yaw
//
//			player.teleport(
//				newLoc,
//				PlayerTeleportEvent.TeleportCause.PLUGIN,
//				*TeleportFlag.Relative.entries.toTypedArray(),
//				TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY,
//				TeleportFlag.EntityState.RETAIN_VEHICLE
//			)
		}

		var highestFrequency = Collections.frequency(vectors, playerDeltaVector)
		for (previousVector in vectors) {
			val frequency = Collections.frequency(vectors, previousVector)
			if (previousVector != playerDeltaVector && frequency > highestFrequency) {
				highestFrequency = frequency
				playerDeltaVector = previousVector
			}
		}

		val forwardZ = direction.modZ != 0
		val strafeAxis = if (forwardZ) playerDeltaVector.x else playerDeltaVector.z
		val strafe = sign(strafeAxis).toInt() * abs(targetSpeed)
		val ascensionAxis = if (forwardZ) playerDeltaVector.z * -direction.modZ else playerDeltaVector.x * -direction.modX
		val ascension = sign(ascensionAxis).toInt() * abs(targetSpeed)
		if (forwardZ) dx += strafe else dz += strafe
		dy += ascension

		val deltaTime = elapsedSinceLastMove / 1000.0
		val maxChange = 15 * starship.reactor.powerDistributor.thrusterPortion * deltaTime

		val directControlVec = directControlVector
		val offset = Vector(dx, dy, dz).subtract(directControlVec)
		if (offset.length() > maxChange) {
			offset.normalize().multiply(maxChange)
		}
		directControlVec.add(offset)
		dx = directControlVec.blockX
		dy = directControlVec.blockY
		dz = directControlVec.blockZ

		dx *= speedFac
		dy *= speedFac
		dz *= speedFac

		when {
			dy < 0 && starship.min.y + dy < 0 -> {
				dy = -starship.min.y
			}

			dy > 0 && starship.max.y + dy > starship.world.maxHeight -> {
				dy = starship.world.maxHeight - starship.max.y
			}
		}

		if (StarshipControl.locationCheck(starship, dx, dy, dz)) {
			return
		}

		if (dx == 0 && dy == 0 && dz == 0) {
			return
		}

		player.walkSpeed = 0.009f
		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	companion object {
		private const val DIRECT_CONTROL_DIVISOR = 1.75

		fun calculateSpeed(selectedSlot: Int) = if (selectedSlot == 0) -1 else (selectedSlot / DIRECT_CONTROL_DIVISOR).toInt()
		fun calculateCooldown(movementCooldown: Long, heldItemSlot: Int) = movementCooldown - heldItemSlot * 8
	}
}
