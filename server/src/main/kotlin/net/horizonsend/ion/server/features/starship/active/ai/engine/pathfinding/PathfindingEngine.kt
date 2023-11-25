package net.horizonsend.ion.server.features.starship.active.ai.engine.pathfinding

import net.horizonsend.ion.server.features.starship.active.ai.engine.AIEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class PathfindingEngine(
	controller: AIController,
	protected val positioningSupplier: Supplier<Vec3i>
) : AIEngine(controller) {
	abstract var blocked: Boolean

	abstract fun getMovementVector(): Vector

	abstract fun getDestination(): Vec3i
}
