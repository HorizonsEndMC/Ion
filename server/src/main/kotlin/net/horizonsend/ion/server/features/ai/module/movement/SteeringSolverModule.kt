package net.horizonsend.ion.server.features.ai.module.movement

import SteeringModule
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.round

class SteeringSolverModule(
    controller: AIController,
    val steeringModule: SteeringModule,
) : AIModule(controller) {

    override fun tick() {
        steeringModule.steer()
        //shiftFlyInDirection(steeringModule.getThrust())
		updateDirectControl()
    }

    fun shiftFlyInDirection(
        direction: Vector,
        stopCruising: Boolean = false
    ) = Tasks.sync {
        val starship = controller.starship as ActiveControlledStarship
        if (stopCruising) StarshipCruising.stopCruising(controller, starship)

        AIControlUtils.shiftFlyInDirection(controller, direction)
    }

	fun updateDirectControl() {
		if (!controller.starship.isDirectControlEnabled)	controller.starship.setDirectControlEnabled(true)
		val thrust = steeringModule.getThrust()
		var heading = steeringModule.getHeading()
		val throttle = steeringModule.getThrottle()
		//map onto player slots
		controller.selectedDirectControlSpeed = 9//round(throttle * 8.0).toInt() + 1
		if (thrust.dot(heading) < 0.0) controller.selectedDirectControlSpeed = 0 //ship wants to go backwards
		AIControlUtils.faceDirection(controller, vectorToBlockFace(heading))
	}

	fun directControlMovementVector(direction: BlockFace) : Vector {
		val thrust = steeringModule.getThrust()
		println("thrust $thrust")

		val forwardX = direction.modZ == 0
		val rotated = thrust.clone()//.multiply(-1.0)
		rotated.y *= 1.5 //stretch y a little so that ship can strafe up and down more easily
		when (direction) {
			BlockFace.NORTH -> rotated.rotateAroundX(PI/2)
			BlockFace.SOUTH -> rotated.rotateAroundX(-PI/2)
			BlockFace.WEST -> rotated.rotateAroundZ(-PI/2)
			BlockFace.EAST -> rotated.rotateAroundZ(PI/2)
			else ->rotated.rotateAroundX(0.0)
		}
		println(rotated)
		rotated.setY(0)
		//if (forwardX) rotated.rotateAroundY(PI/4) // Z -> X
		if (rotated.lengthSquared() < 1e-2) return Vector(0.0,0.0,0.0)
		rotated.normalize()
		println(rotated)

		rotated.x = round(rotated.x)
		rotated.z = round(rotated.z)

		return rotated
	}
}
