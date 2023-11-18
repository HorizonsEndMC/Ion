package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier

abstract class PathfindingEngine(
	controller: AIController,
	protected val positioningSupplier: Supplier<Vec3i>
) : AIEngine(controller) {
	abstract var blocked: Boolean

	abstract fun getFirstNavPoint(): Vec3i

	abstract fun getDestination(): Vec3i
}
