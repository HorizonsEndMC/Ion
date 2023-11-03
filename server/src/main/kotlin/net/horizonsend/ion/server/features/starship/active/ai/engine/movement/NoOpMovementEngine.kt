package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController

class NoOpMovementEngine(controller: ActiveAIController, pathfindingEngine: PathfindingEngine) : MovementEngine(controller, pathfindingEngine) {
	override fun tick() {}
}
