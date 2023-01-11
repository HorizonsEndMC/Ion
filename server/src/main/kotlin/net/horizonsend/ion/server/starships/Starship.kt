package net.horizonsend.ion.server.starships

import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.mainThreadCheck
import net.horizonsend.ion.server.starships.control.Controller
import net.horizonsend.ion.server.starships.control.PlayerController
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.server.level.ServerLevel
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.movement.TranslateMovement
import org.bukkit.Bukkit

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

	var velocityX = 0.0; private set
	var velocityY = 0.0; private set
	var velocityZ = 0.0; private set

	open fun tick() {
		mainThreadCheck()

		if (Bukkit.getCurrentTick() % 20 != 0) return

		val (forwardBackward, upDown, rightLeft) = controller?.accelerationTick() ?: return

		val forwardBackwardThrust = if (forwardBackward == 1) frontThrust else if (forwardBackward == -1) backThrust else 0.0
		val upDownThrust = if (upDown == 1) upThrust else if (upDown == -1) downThrust else 0.0
		val rightLeftThrust = if (rightLeft == 1) rightThrust else if (rightLeft == -1) leftThrust else 0.0

		var (relativeVelocityForwardBackward, relativeVelocityUpDown, relativeVelocityRightLeft) = globalToRelative(velocityX, velocityY, velocityZ)

		relativeVelocityForwardBackward += forwardBackwardThrust
		relativeVelocityUpDown += upDownThrust
		relativeVelocityRightLeft += rightLeftThrust

		val (x, y, z) = relativeToGlobal(forwardBackward, upDown, rightLeft)
		velocityX += x
		velocityY += y
		velocityZ += z

		TranslateMovement.loadChunksAndMove((this as? ActiveStarship) ?: return, velocityX.toInt(), velocityY.toInt(), velocityZ.toInt(), null)
	}

	fun cleanup() {
		mainThreadCheck()

		controller?.cleanup()
	}

	// region Temporary code
	private val naturalDeceleration = 1.0

	protected open val frontThrust = 1.0
	protected open val backThrust = 1.0
	protected open val leftThrust = 1.0
	protected open val rightThrust = 1.0
	protected open val upThrust = 1.0
	protected open val downThrust = 1.0

	protected open val frontLimit = 8.0
	protected open val backLimit = 8.0
	protected open val leftLimit = 8.0
	protected open val rightLimit = 8.0
	protected open val upLimit = 8.0
	protected open val downLimit = 8.0
	// endregion

	fun globalToRelative(x: Int, y: Int, z: Int): Triple<Int, Int, Int> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(-z, y, x)
			Direction.SOUTH -> Triple(z, y, -x)
			Direction.WEST -> Triple(-x, y, -z)
			Direction.EAST -> Triple(x, y, z)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun globalToRelative(x: Double, y: Double, z: Double): Triple<Double, Double, Double> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(-z, y, x)
			Direction.SOUTH -> Triple(z, y, -x)
			Direction.WEST -> Triple(-x, y, -z)
			Direction.EAST -> Triple(x, y, z)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	fun relativeToGlobal(forwardBackward: Int, upDown: Int, rightLeft: Int): Triple<Int, Int, Int> {
		return when (facingDirection) {
			Direction.NORTH -> Triple(rightLeft, upDown, -forwardBackward)
			Direction.SOUTH -> Triple(-rightLeft, upDown, forwardBackward)
			Direction.WEST -> Triple(-forwardBackward, upDown, -rightLeft)
			Direction.EAST -> Triple(forwardBackward, upDown, rightLeft)
			else -> throw IllegalStateException("Ship direction must not be vertical")
		}
	}

	override fun audience(): Audience = controller as? PlayerController ?: Audience.empty()
}
