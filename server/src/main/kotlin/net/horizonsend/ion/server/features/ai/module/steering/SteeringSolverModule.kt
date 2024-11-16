package net.horizonsend.ion.server.features.ai.module.steering

import SteeringModule
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.debug.AIDebugModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sign

class SteeringSolverModule(
	controller: AIController,
	private val steeringModule: SteeringModule,
	val difficulty : DifficultyModule,
	val target: Supplier<AITarget?>,
	val type : MovementType = MovementType.CRUISE
) : AIModule(controller) {

	val ship : Starship get() = controller.starship
	private var thrust = Vector()
	private var throttle = 0.0
	private var heading = Vector()

    override fun tick() {
        steeringModule.steer()

		thrust = steeringModule.thrustOut
		heading = diagonalFixed(steeringModule.headingOut)
		throttle = steeringModule.throttleOut

		if (AIDebugModule.canShipsRotate) {
			AIControlUtils.faceDirection(controller, vectorToBlockFace(heading))
		}
		if (!AIDebugModule.canShipsMove) {
			controller.starship.setDirectControlEnabled(false)
			StarshipCruising.stopCruising(controller,starship)
			controller.isSneakFlying = false
			return
		}

		val targetVec = target.get()?.getLocation()?.toVector()
		val targetDist = targetVec?.add(ship.centerOfMass.toVector().multiply(-1.0))?.length() ?: 1e10

		if (type == MovementType.DC && targetDist < 700.0) {
			updateDirectControl()
		} else {
			handleCruise()
		}
    }

    private fun shiftFlyInDirection(direction: Vector) = Tasks.sync {
        AIControlUtils.shiftFlyInDirection(controller, direction)
    }

	private fun updateDirectControl() {
		if (!controller.starship.isDirectControlEnabled)	controller.starship.setDirectControlEnabled(true)
		controller.isSneakFlying = !difficulty.speedDebuff

		//map onto player slots
		controller.selectedDirectControlSpeed = round(throttle * 8.0).toInt() + 1
		if (thrust.dot(ship.forward.direction) < 0.0) controller.selectedDirectControlSpeed = 0 //ship wants to go backwards
	}

	fun directControlMovementVector(direction: BlockFace) : Vector {
		val thrust = steeringModule.thrustOut

		val rotated = thrust.clone()//.multiply(-1.0)
		rotated.y *= 1.5 //stretch y a little so that ship can strafe up and down more easily
		when (direction) {
			BlockFace.NORTH -> rotated.rotateAroundX(PI/2)
			BlockFace.SOUTH -> rotated.rotateAroundX(-PI/2)
			BlockFace.WEST -> rotated.rotateAroundZ(-PI/2)
			BlockFace.EAST -> rotated.rotateAroundZ(PI/2)
			else ->rotated.rotateAroundX(0.0)
		}
		rotated.setY(0)
		//if (forwardX) rotated.rotateAroundY(PI/4) // Z -> X
		if (rotated.lengthSquared() < 1e-2) return Vector(0.0,0.0,0.0)
		rotated.normalize()

		rotated.x = round(rotated.x)
		rotated.z = round(rotated.z)

		return rotated
	}

	private fun handleCruise() {
		if (controller.starship.isDirectControlEnabled)	controller.starship.setDirectControlEnabled(false)

		var onPlane = thrust.clone()
		onPlane.setY(0)
		onPlane.normalize()

		onPlane = diagonalFixed(onPlane)


		val dx = if (abs(onPlane.x) >= 0.5) sign(thrust.x).toInt() else 0
		val dz = if (abs(onPlane.z) > 0.5) sign(thrust.z).toInt() else 0

		if (dx == 0 && dz == 0) { // moving up or down
			StarshipCruising.stopCruising(controller,starship)
			if (throttle > 0.5) shiftFlyInDirection(thrust) else AIControlUtils.shiftFlyInDirection(controller,null)
			return
		}

		var (_, maxSpeed) = starship.getThrustData(dx, dz)
		maxSpeed /= 2
		if (difficulty.speedDebuff) {
			maxSpeed = round(0.7 * maxSpeed).toInt()
		}

		maxSpeed = (maxSpeed * starship.balancing.cruiseSpeedMultiplier).toInt()
		val finalSpeed = round(throttle * maxSpeed).toInt()
		starship.speedLimit = finalSpeed


		StarshipCruising.startCruising(controller, starship,onPlane)

		if (finalSpeed > starship.balancing.maxSneakFlyAccel) shiftFlyInDirection(thrust)
	}

	/** to prevent overloading the rotation queue on diagonals fix the diagonal slightly
	 * such that the ship does not rotate unless the heading is significantly different*/
	private fun diagonalFixed(dir : Vector) : Vector{
		val heading = dir.clone()
		val (_, headingYaw) = vectorToPitchYaw(heading, true)
		val (_, shipYaw) = vectorToPitchYaw(starship.getTargetForward().direction, true)

		val angle = headingYaw-shipYaw

		if ( ((PI / 4) * 0.8 < abs(angle)) && (abs(angle) < (PI / 4) * 1.2 )) {
			heading.rotateAroundY(sign(angle) * max(0.0, abs(angle) - (PI / 4) * 0.8))
		}

		return heading
	}

	enum class MovementType {DC, CRUISE}
}
