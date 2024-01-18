package net.horizonsend.ion.server.features.starship.ai.module.pathfinding

import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.util.Vector
import java.util.function.Supplier

abstract class PathfindingModule(
	controller: AIController,
	protected val positioningSupplier: Supplier<Vec3i?>
) : AIModule(controller) {
	abstract fun getMovementVector(): Vector

	abstract fun getDestination(): Vec3i?

	open fun getIsBlocked(): Boolean = controller.hasBeenBlockedWithin(50)
}
