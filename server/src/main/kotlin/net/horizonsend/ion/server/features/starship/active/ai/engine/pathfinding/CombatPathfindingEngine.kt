package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

class CombatPathfindingEngine(
	controller: AIController,
	destinationSupplier: PositioningEngine
) : PathfindingEngine(controller, destinationSupplier) {
	override fun shouldNotPathfind(newCenter: Vec3i): Boolean = false
}
