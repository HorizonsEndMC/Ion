package net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces

import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding.PathfindingEngine
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine

interface ActiveAIController {
	val positioningEngine: PositioningEngine
	val pathfindingEngine: PathfindingEngine
	val movementEngine: MovementEngine
}
