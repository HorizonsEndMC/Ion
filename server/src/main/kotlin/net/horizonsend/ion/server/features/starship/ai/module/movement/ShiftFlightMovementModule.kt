package net.horizonsend.ion.server.features.starship.ai.module.movement

import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.PathfindingModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

class ShiftFlightMovementModule(
	controller: AIController,
	pathfindingModule: PathfindingModule,
) : MovementModule(controller, pathfindingModule) {
	override fun tick() {
		Tasks.sync {
			stopCruising()
			shiftFly(starshipLocation.toLocation(world), false)
		}
	}
}
