package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier

class SteeringPathfindingEngine(
	controller: AIController,
	positioningSupplier: Supplier<Vec3i>
) : PathfindingEngine(controller, positioningSupplier) {
	val blockedPositions: ArrayDeque<Vec3i> = ArrayDeque(50)
	override var blocked: Boolean = false

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		if (location == null) return

		blockedPositions.addFirst(location)
	}

	override fun getFirstNavPoint(): Vec3i {
		return positioningSupplier.get()
//		return blockedPositions.firstOrNull() ?: getCenterVec3i()
	}

	override fun getDestination(): Vec3i {
		return getFirstNavPoint()
	}
}
