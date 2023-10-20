package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.movement.MovementEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class PathfindIfBlockedEngine(
	controller: AIController,
	destination: Vec3i?
) : PathfindingEngine(controller, destination) {
	private val blocked get() = controller.blocked || predictBlocked()

	override fun navigate(): Future<*> {
		if (!blocked) return CompletableFuture.completedFuture(Any())

		return super.navigate()
	}

	/** Check if the projected section is blocked */
	@Synchronized
	private fun predictBlocked(): Boolean {
		starship as ActiveControlledStarship
		val cruiseDir = starship.cruiseData.velocity

		val (x, y, z) = Vec3i(getCenter().clone().add(cruiseDir))
		val projectedSection = Vec3i(x.shr(4), y.shr(4), z.shr(4))

		return trackedSections
			.filter { it.position == projectedSection }
			.any { !it.navigable }
	}

	override fun getFirstNavPoint(): Vec3i? {
		return if (blocked) super.getFirstNavPoint() else destination
	}

	override fun passToMovementEngine(movementEngine: MovementEngine) {
		if (!blocked) return

		super.passToMovementEngine(movementEngine)
	}
}
