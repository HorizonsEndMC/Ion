package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class PathfindIfBlockedEngineAStar(
	controller: AIController,
	destinationSupplier: Supplier<Vec3i>
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

	override fun getMovementVector(): Vector {
		return if (blocked) super.getMovementVector() else positioningSupplier.get().toVector().subtract(getCenter().toVector()).normalize()
	}
}
