package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks

class ShiftFlightMovementEngine(
	controller: AIController,
	pathfindingEngine: PathfindingEngine,
) : MovementEngine(controller, pathfindingEngine) {
	override fun tick() {
		Tasks.sync {
			stopCruising()
			shiftFly(starshipLocation.toLocation(world), false)
		}
	}
}
