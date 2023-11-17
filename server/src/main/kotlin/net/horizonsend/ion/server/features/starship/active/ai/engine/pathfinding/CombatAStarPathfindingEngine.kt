package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class CombatAStarPathfindingEngine(
	controller: AIController,
	destinationSupplier: PositioningEngine
) : AStarPathfindingEngine(controller, destinationSupplier) {
	override fun shouldNotPathfind(newCenter: Vec3i): Boolean = false
}
