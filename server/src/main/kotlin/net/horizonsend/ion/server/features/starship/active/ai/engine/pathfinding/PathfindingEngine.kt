package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.interfaces.ActiveAIController

class PathfindingEngineAStarPathfindingEngine(
	controller: ActiveAIController,
	protected val positioningSupplier: PositioningEngine
) {

}
