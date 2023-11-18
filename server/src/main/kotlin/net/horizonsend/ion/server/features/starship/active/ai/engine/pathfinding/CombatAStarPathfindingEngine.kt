package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier

class CombatAStarPathfindingEngine(
	controller: AIController,
	destinationSupplier: Supplier<Vec3i>
) : AStarPathfindingEngine(controller, destinationSupplier) {
	override fun shouldNotPathfind(newCenter: Vec3i): Boolean = false
}
