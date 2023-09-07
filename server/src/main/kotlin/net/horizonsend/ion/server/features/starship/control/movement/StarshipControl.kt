package net.horizonsend.ion.server.features.starship.control.movement

import io.papermc.paper.entity.TeleportFlag
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.nations.utils.getPing
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType.PLATFORM
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.StarshipTeleportation
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.util.Vector
import java.util.Collections
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin

object StarshipControl : IonServerComponent() {
	val CONTROLLER_TYPE = Material.CLOCK
	const val DIRECT_CONTROL_DIVISOR = 1.75

	override fun onEnable() {
		Tasks.syncRepeat(1, 1) { ActiveStarships.allControlledStarships().forEach(::processManualFlight) }
	}

	private fun processManualFlight(starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			starship.controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (starship.isTeleporting) return

		val controller = starship.controller ?: return

		if (starship.isDirectControlEnabled) {
			processDirectControl(starship)
			return
		}

		if (controller.isShiftFlying) {
			processSneakFlight(controller, starship)
		}
	}

	private fun processDirectControl(starship: ActiveControlledStarship) {
		val controller = starship.controller ?: return

		if (starship.type == PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) {
			starship.setDirectControlEnabled(false)
			return
		}

		val playerPilot = starship.playerPilot ?: return //TODO

		val ping = playerPilot?.let { getPing(playerPilot) } ?: 0
		val movementCooldown = starship.directControlCooldown
		val speedFac = if (ping > movementCooldown) 2 else 1

		val selectedSpeed = controller.selectedDirectControlSpeed

		val cooldown = calculateCooldown(movementCooldown, selectedSpeed) * speedFac
		val currentTime = System.currentTimeMillis()
		val lastManualMove = starship.lastManualMove

		val elapsedSinceLastMove = currentTime - lastManualMove
		if (elapsedSinceLastMove < cooldown) return

		starship.lastManualMove = currentTime

		var dx = 0
		var dy = 0
		var dz = 0

		val direction = starship.getTargetForward()
		val targetSpeed = calculateSpeed(selectedSpeed)

		dx += (targetSpeed * direction.modX)
		dz += (targetSpeed * direction.modZ)

		if (controller.isShiftFlying) {
			dx *= 2
			dz *= 2
		}

		var center = starship.directControlCenter
		if (center == null) {
			center = playerPilot.location.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}
		var vector = playerPilot.location.toVector().subtract(center.toVector())
		vector.setY(0)
		vector.normalize()

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

		val vectors = starship.directControlPreviousVectors
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
				*TeleportFlag.Relative.values(),
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

		val directControlVec = starship.directControlVector
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

		if (locationCheck(starship, dx, dy, dz)) {
			return
		}

		if (dx == 0 && dy == 0 && dz == 0) {
			return
		}

		playerPilot.walkSpeed = 0.009f
		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	fun calculateSpeed(selectedSlot: Int) = if (selectedSlot == 0) -1 else (selectedSlot / DIRECT_CONTROL_DIVISOR).toInt()
	fun calculateCooldown(movementCooldown: Long, heldItemSlot: Int) = movementCooldown - heldItemSlot * 8

	private fun processSneakFlight(controller: Controller, starship: ActiveControlledStarship) {
		if (starship.type == PLATFORM) {
			controller.userErrorAction("This ship type is not capable of moving.")
			return
		}

		if (Hyperspace.isWarmingUp(starship)) {
			starship.controller.userErrorAction("Cannot move while in hyperspace warmup.")
			return
		}

		if (!controller.isShiftFlying) return

		val now = System.currentTimeMillis()
		if (now - starship.lastManualMove < starship.manualMoveCooldownMillis) return

		starship.lastManualMove = now
		starship.sneakMovements++

		val sneakMovements = starship.sneakMovements

		val maxAccel = starship.data.starshipType.actualType.maxSneakFlyAccel
		val accelDistance = starship.data.starshipType.actualType.sneakFlyAccelDistance

		val yawRadians = Math.toRadians(controller.yaw.toDouble())
		val pitchRadians = Math.toRadians(controller.pitch.toDouble())

		val distance = max(min(maxAccel, sneakMovements / min(1, accelDistance)), 1)

		val vertical = abs(pitchRadians) >= PI * 5 / 12 // 75 degrees

		val dx = if (vertical) 0 else sin(-yawRadians).roundToInt() * distance
		val dy = sin(-pitchRadians).roundToInt() * distance
		val dz = if (vertical) 0 else cos(yawRadians).roundToInt() * distance

		if (locationCheck(starship, dx, dy, dz)) return

		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz)
	}

	fun locationCheck(starship: ActiveControlledStarship, dx: Int, dy: Int, dz: Int): Boolean {
		val world = starship.world
		val newCenter = starship.centerOfMass.toLocation(world).add(dx.d(), dy.d(), dz.d())

		val planet = Space.getPlanets().asSequence()
			.filter { it.spaceWorld == world }
			.filter {
				it.location.toLocation(world).distanceSquared(newCenter) < starship.getEntryRange(it).toDouble().pow(2)
			}
			.firstOrNull()
			?: return false

		val border = planet.planetWorld?.worldBorder
			?.takeIf { it.size < 60_000_000 } // don't use if it's the default, giant border
		val halfLength = if (border == null) 2500.0 else border.size / 2.0
		val centerX = border?.center?.x ?: halfLength
		val centerZ = border?.center?.z ?: halfLength

		val distance = (halfLength - 250) * max(0.15, newCenter.y / starship.world.maxHeight)
		val offset = newCenter.toVector()
			.subtract(planet.location.toVector())
			.normalize().multiply(distance)

		val x = centerX + offset.x
		val y = 250.0 - (starship.max.y - starship.min.y)
		val z = centerZ + offset.z
		val target = Location(planet.planetWorld, x, y, z).toBlockLocation()

		starship.sendMessage(
			text()
				.color(NamedTextColor.GRAY)
				.decorate(TextDecoration.ITALIC)
				.append(text("Entering "))
				.append(text(planet.name, NamedTextColor.BLUE))
				.append(text("..."))
		)

		StarshipTeleportation.teleportStarship(starship, target)
		return true
	}

	private fun accel(old: Double, new: Double, maxChange: Double): Double {
		val diff = new - old
		if (diff < maxChange) {
			return new
		}

		return old + min(maxChange, abs(diff)) * sign(diff)
	}
}
