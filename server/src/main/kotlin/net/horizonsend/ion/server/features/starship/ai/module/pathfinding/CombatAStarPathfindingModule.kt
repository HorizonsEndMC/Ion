package net.horizonsend.ion.server.features.starship.ai.module.pathfinding

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier

class CombatAStarPathfindingModule(
	controller: AIController,
	destinationSupplier: Supplier<Vec3i>
) : AStarPathfindingModule(controller, destinationSupplier) {
	override fun shouldNotPathfind(newCenter: Vec3i): Boolean = false
}
