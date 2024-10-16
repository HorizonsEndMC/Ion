package net.horizonsend.ion.server.features.starship.control.input

import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.utils.getPing
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.control.movement.StarshipControl
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.kyori.adventure.text.Component.keybind
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.entity.Player
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

		val player: Player = (controller as? PlayerController)?.player ?: return
		player.walkSpeed = 0.2f // default
	}

	override fun handlePlayerHoldItem(event: PlayerItemHeldEvent) {
		val player = event.player
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

		val playerPilot = controller.player

		// Ping compensation
		val ping = getPing(playerPilot)
		val movementCooldown = starship.directControlCooldown
		val playerDcModifier = PlayerCache[playerPilot.uniqueId].dcSpeedModifier
		val speedFac = if (ping > movementCooldown) max(2, playerDcModifier) else playerDcModifier

		val selectedSpeed = (controller.selectedDirectControlSpeed * starship.directControlSpeedModifier).toInt().coerceAtLeast(0)

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

		val direction = starship.getTargetForward()
		val targetSpeed = calculateSpeed(selectedSpeed)

		dx += (targetSpeed * direction.modX)
		dz += (targetSpeed * direction.modZ)

		// Boost if shift flying
		if (controller.isSneakFlying()) {
			dx *= 2
			dz *= 2
		}

		// Use the player's location if available, otherwise the computer loc
		val pilotLocation = playerPilot.location

		var center = starship.directControlCenter
		if (center == null) {
			center = pilotLocation.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}

		// Calculate the movement vector
		var vector = pilotLocation.toVector().subtract(center.toVector())
		vector.setY(0)
		vector.normalize()

		// Clone the vector to do some additional math
		val directionWrapper = center.clone()
		directionWrapper.direction = Vector(direction.modX, direction.modY, direction.modZ)

		val playerDirectionWrapper = center.clone()
		playerDirectionWrapper.direction = playerPilot.location.direction

		val vectorWrapper = center.clone()
		vectorWrapper.direction = vector

		vectorWrapper.yaw = vectorWrapper.yaw - (playerDirectionWrapper.yaw - directionWrapper.yaw)
		vector = vectorWrapper.direction

		vector.x = round(vector.x)
		vector.setY(0)
		vector.z = round(vector.z)

		val vectors = directControlPreviousVectors
		if (vectors.size > 3) {
			vectors.poll()
		}
		vectors.add(vector)


		if (vector.x != 0.0 || vector.z != 0.0) {
			val newLoc = center.clone()

			newLoc.pitch = playerPilot.location.pitch
			newLoc.yaw = playerPilot.location.yaw

			playerPilot.teleport(
				newLoc,
				PlayerTeleportEvent.TeleportCause.PLUGIN,
				*TeleportFlag.Relative.entries.toTypedArray(),
				TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY
			)
		}

		var highestFrequency = Collections.frequency(vectors, vector)
		for (previousVector in vectors) {
			val frequency = Collections.frequency(vectors, previousVector)
			if (previousVector != vector && frequency > highestFrequency) {
				highestFrequency = frequency
				vector = previousVector
			}
		}

		val forwardZ = direction.modZ != 0
		val strafeAxis = if (forwardZ) vector.x else vector.z
		val strafe = sign(strafeAxis).toInt() * abs(targetSpeed)
		val ascensionAxis = if (forwardZ) vector.z * -direction.modZ else vector.x * -direction.modX
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

		playerPilot.walkSpeed = 0.009f
		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	companion object {
		private const val DIRECT_CONTROL_DIVISOR = 1.75

		fun calculateSpeed(selectedSlot: Int) = if (selectedSlot == 0) -1 else (selectedSlot / DIRECT_CONTROL_DIVISOR).toInt()
		fun calculateCooldown(movementCooldown: Long, heldItemSlot: Int) = movementCooldown - heldItemSlot * 8
	}
}
