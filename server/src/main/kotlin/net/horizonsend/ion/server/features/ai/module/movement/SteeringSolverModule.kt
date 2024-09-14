package net.horizonsend.ion.server.features.ai.module.movement

import SteeringModule
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.features.starship.control.movement.StarshipCruising
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class SteeringSolverModule(
    controller: AIController,
    val steeringModule: SteeringModule,
) : AIModule(controller) {

    override fun tick() {
        steeringModule.steer()
        shiftFlyInDirection(steeringModule.getThrust())
    }

    fun shiftFlyInDirection(
        direction: Vector,
        stopCruising: Boolean = false
    ) = Tasks.sync {
        val starship = controller.starship as ActiveControlledStarship
        if (stopCruising) StarshipCruising.stopCruising(controller, starship)

        AIControlUtils.shiftFlyInDirection(controller, direction)
    }
}
