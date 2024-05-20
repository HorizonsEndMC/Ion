package net.horizonsend.ion.server.features.ai.module.pathfinding

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.average
import org.bukkit.util.Vector
import java.util.concurrent.LinkedBlockingDeque
import java.util.function.Supplier

open class SteeringPathfindingModule(
	controller: AIController,
	positioningSupplier: Supplier<Vec3i?>
) : PathfindingModule(controller, positioningSupplier) {
	protected val avoidPositions: LinkedBlockingDeque<Vec3i> = LinkedBlockingDeque(50)

	fun getAvoidVector(): Vector {
		val center = getCenter().toVector()
		val vectors = avoidPositions.map { it.toVector().subtract(center) }

		return vectors.average()
	}

	override fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {
		if (location == null) return

		avoidPositions.offerFirst(location)
	}

	override fun getMovementVector(): Vector {
		val destination = getDestination() ?: return Vector()
		val center = getCenter().toVector()
		val movementDirection = destination.toVector().subtract(center)

		return movementDirection.subtract(getAvoidVector())
	}

	override fun getDestination(): Vec3i? {
		return positioningSupplier.get()
	}
}
