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

			(value as? PlayerController)?.sendInformation("Switched active control mode to ${value.name}.")
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

		// Apply deceleration
		if (velocityX > 1 / 20) velocityX -= 1 / 20 else if (velocityX < -(1 / 20)) velocityX += 1 / 20 else velocityX = 0.0
		if (velocityY > 1 / 20) velocityY -= 1 / 20 else if (velocityY < -(1 / 20)) velocityY += 1 / 20 else velocityY = 0.0
		if (velocityZ > 1 / 20) velocityZ -= 1 / 20 else if (velocityZ < -(1 / 20)) velocityZ += 1 / 20 else velocityZ = 0.0

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

	fun localToGlobal(frontBack: Int, upDown: Int, rightLeft: Int): Triple<Int, Int, Int> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(rightLeft, upDown, -frontBack)
			Direction.SOUTH -> Triple(-rightLeft, upDown, frontBack)
			Direction.WEST -> Triple(-frontBack, upDown, -rightLeft)
			Direction.EAST -> Triple(frontBack, upDown, rightLeft)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun localToGlobal(frontBack: Double, upDown: Double, rightLeft: Double): Triple<Double, Double, Double> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(rightLeft, upDown, -frontBack)
			Direction.SOUTH -> Triple(-rightLeft, upDown, frontBack)
			Direction.WEST -> Triple(-frontBack, upDown, -rightLeft)
			Direction.EAST -> Triple(frontBack, upDown, rightLeft)
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

	override fun audience(): Audience = controller as? PlayerController ?: Audience.empty()
}
