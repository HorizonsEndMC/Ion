package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.positioning.PositioningEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.concurrent.CompletableFuture

class PathfindIfBlockedEngineAStar(
	controller: AIController,
	destinationSupplier: PositioningEngine
) : AStarPathfindingEngine(controller, destinationSupplier) {
	override var tickInterval: Int = 1
	override var blocked = false; get() = controller.hasBeenBlockedWithin() || predictBlocked()

	override fun navigate(): CompletableFuture<*> {
		if (!blocked) return CompletableFuture.completedFuture(Any())

		return super.navigate()
	}

	/** Check if the projected section is blocked */
	@Synchronized
	private fun predictBlocked(): Boolean {
		val cruiseDir = (starship as ActiveControlledStarship).cruiseData.velocity

		val (x, y, z) = Vec3i(getCenter().clone().add(cruiseDir))
		val projectedSection = Vec3i(x.shr(4), y.shr(4), z.shr(4))

		return trackedSections
			.filter { it.position == projectedSection }
			.any { !it.navigable }
	}

	override fun getFirstNavPoint(): Vec3i {
		return if (blocked) super.getFirstNavPoint() else positioningSupplier.getDestination()
	}
}
