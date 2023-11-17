package net.horizonsend.ion.server.features.starship.active.ai.engine.movement

import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.AStarPathfindingEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController

class NoOpMovementEngine(controller: AIController, pathfindingEngine: AStarPathfindingEngine) : MovementEngine(controller, pathfindingEngine) {
	override fun tick() {}
}
