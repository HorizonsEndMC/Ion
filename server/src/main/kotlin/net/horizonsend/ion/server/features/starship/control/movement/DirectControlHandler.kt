package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.utils.getPing
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.control.input.DirectControlInput
import net.horizonsend.ion.server.features.starship.control.input.PlayerInput
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import org.bukkit.util.Vector
import java.util.Collections
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

class DirectControlHandler(controller: Controller, override val input: DirectControlInput
) : MovementHandler(controller, "Direct Control",	input) {
	private val directControlPreviousVectors = LinkedBlockingQueue<Vector>(4)
	private val directControlVector = Vector()

	override fun create() {
		input.create()
	}

	override fun destroy() {
		input.destroy()
		directControlVector.x = 0.0
		directControlVector.y = 0.0
		directControlVector.z = 0.0

	}

	override fun tick() {
		if (starship.isTeleporting) return

		if (starship.type == StarshipType.PLATFORM) return controller.userErrorAction("This ship type is not capable of moving.")

		if (Hyperspace.isWarmingUp(starship) || Hyperspace.isMoving(starship)) {
			starship.setDirectControlEnabled(false)
			return
		}

		var speedFac = 1
		val movementCooldown = starship.directControlCooldown
		if (input is PlayerInput) {
			// Ping compensation
			val ping = getPing(input.player)
			val playerDcModifier = PlayerCache[input.player.uniqueId].dcSpeedModifier
			speedFac = if (ping > movementCooldown) max(2, playerDcModifier) else playerDcModifier
		}

		val data = input.getData()

		val cooldown = calculateCooldown(movementCooldown, data.selectedSpeed) * speedFac
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
		val targetSpeed = (calculateSpeed(data.selectedSpeed) * starship.directControlSpeedModifierFromIonTurrets *
				starship.directControlSpeedModifierFromHeavyLasers)

		// Initialize forward movement
		dx += (targetSpeed * direction.modX).toInt()
		dz += (targetSpeed * direction.modZ).toInt()

		// Boost if shift flying
		if (data.isBoosting) {
			dx *= 2
			dz *= 2
		}

		var strafeVector = data.strafeVector


		val vectors = directControlPreviousVectors
		if (vectors.size > 3) {
			vectors.poll()
		}
		// Store strafe vectors
		vectors.add(strafeVector)


		var highestFrequency = Collections.frequency(vectors, strafeVector)
		for (previousVector in vectors) {
			val frequency = Collections.frequency(vectors, previousVector)
			if (previousVector != strafeVector && frequency > highestFrequency) {
				highestFrequency = frequency
				strafeVector = previousVector
			}
		}

		val forwardZ = direction.modZ != 0
		val strafeAxis = if (forwardZ) strafeVector.x else strafeVector.z
		val strafe = (sign(strafeAxis).toInt() * abs(targetSpeed)).toInt()
		val ascensionAxis = if (forwardZ) strafeVector.z * -direction.modZ else strafeVector.x * -direction.modX
		val ascension = (sign(ascensionAxis).toInt() * abs(targetSpeed)).toInt()
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

		var maxHeight = starship.world.maxHeight
		if (starship.world.hasFlag(WorldFlag.SPACE_WORLD)) maxHeight -= 1

		when {
			dy < 0 && starship.min.y + dy < 0 -> {
				dy = -starship.min.y
			}

			dy > 0 && starship.max.y + dy >= maxHeight -> {
				dy = maxHeight - starship.max.y
			}
		}

		if (StarshipControl.locationCheck(starship, dx, dy, dz)) {
			return
		}

		if (dx == 0 && dy == 0 && dz == 0) {
			return
		}
		TranslateMovement.loadChunksAndMove(starship, dx, dy, dz, type = TranslateMovement.MovementType.DC)
	}

	companion object {
		private const val DIRECT_CONTROL_DIVISOR = 1.75

		fun calculateSpeed(selectedSlot: Int) = if (selectedSlot == 0) -1.0 else (selectedSlot / DIRECT_CONTROL_DIVISOR).toDouble()
		fun calculateCooldown(movementCooldown: Long, heldItemSlot: Int) = movementCooldown - heldItemSlot * 8
	}
}
