package net.horizonsend.ion.server.starships

import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.mainThreadCheck
import net.horizonsend.ion.server.starships.control.Controller
import net.horizonsend.ion.server.starships.control.PlayerController
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.movement.TranslateMovement

open class Starship(
	open var serverLevel: ServerLevel
) : ForwardingAudience.Single {
	open var facingDirection = Direction.NORTH
		set(value) {
			mainThreadCheck()

			if (value.axis == Axis.Y) throw IllegalArgumentException("Ship direction must not be vertical")
			field = value
		}

	open var controller: Controller? = null
		set(value) {
			mainThreadCheck()

			value?.sendInformation("Switched active control mode to ${value.name}.")
			field?.cleanup()
			field = value
		}

	private var velocityX = 0.0
	private var velocityY = 0.0
	private var velocityZ = 0.0

	private var translateX = 0.0
	private var translateY = 0.0
	private var translateZ = 0.0

	open fun tick() {
		mainThreadCheck()

		val decel = 0.05

		// Apply deceleration
		if (velocityX > decel) velocityX -= decel else if (velocityX < -decel) velocityX += decel else velocityX = 0.0
		if (velocityY > decel) velocityY -= decel else if (velocityY < -decel) velocityY += decel else velocityY = 0.0
		if (velocityZ > decel) velocityZ -= decel else if (velocityZ < -decel) velocityZ += decel else velocityZ = 0.0

		// Tick Controller - This is done here because it may apply acceleration
		controller?.tick()

		// Apply velocity to the next translation
		translateX += velocityX
		translateY += velocityY
		translateZ += velocityZ

		// Currently hardcoded to once per second, we can make this variable later
		if (MinecraftServer.currentTick % 20 != 0) return

		// Get a truncated move amount
		val moveX = translateX.toInt()
		val moveY = translateY.toInt()
		val moveZ = translateZ.toInt()

		// Save the remaining decimal amount for the next movement
		translateX -= moveX
		translateY -= moveY
		translateZ -= moveZ

		// Move
		TranslateMovement.loadChunksAndMove((this as? ActiveStarship) ?: return, moveX, moveY, moveZ, null)
	}

	fun cleanup() {
		mainThreadCheck()

		controller?.cleanup()
	}

	fun globalToLocal(x: Int, y: Int, z: Int): Triple<Int, Int, Int> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(-z, y, x)
			Direction.SOUTH -> Triple(z, y, -x)
			Direction.WEST -> Triple(-x, y, -z)
			Direction.EAST -> Triple(x, y, z)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun globalToLocal(x: Double, y: Double, z: Double): Triple<Double, Double, Double> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(-z, y, x)
			Direction.SOUTH -> Triple(z, y, -x)
			Direction.WEST -> Triple(-x, y, -z)
			Direction.EAST -> Triple(x, y, z)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun localToGlobal(frontBack: Int, upDown: Int, leftRight: Int): Triple<Int, Int, Int> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(leftRight, upDown, -frontBack)
			Direction.SOUTH -> Triple(-leftRight, upDown, frontBack)
			Direction.WEST -> Triple(-frontBack, upDown, -leftRight)
			Direction.EAST -> Triple(frontBack, upDown, leftRight)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun localToGlobal(frontBack: Double, upDown: Double, leftRight: Double): Triple<Double, Double, Double> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(leftRight, upDown, -frontBack)
			Direction.SOUTH -> Triple(-leftRight, upDown, frontBack)
			Direction.WEST -> Triple(-frontBack, upDown, -leftRight)
			Direction.EAST -> Triple(frontBack, upDown, leftRight)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun getLocalVelocity() = globalToLocal(velocityX, velocityY, velocityZ)

	fun getGlobalVelocity() = Triple(velocityX, velocityY, velocityZ)

	fun applyGlobalAcceleration(x: Double, y: Double, z: Double) {
		mainThreadCheck()

		velocityX += x
		velocityY += y
		velocityZ += z
	}

	fun applyLocalAcceleration(frontBack: Double, upDown: Double, rightLeft: Double) {
		mainThreadCheck()

		val (x, y, z) = localToGlobal(frontBack, upDown, rightLeft)
		velocityX += x
		velocityY += y
		velocityZ += z
	}

	// TODO: Currently intended to be overriden by ActiveStarship
	open fun getThrustInLocalDirection(direction: Direction): Double = 0.1

	// TODO: Currently intended to be overriden by ActiveStarship
	open fun getSpeedInLocalDirection(direction: Direction): Double = 1.5

	override fun audience(): Audience = controller as? PlayerController ?: Audience.empty()
}
