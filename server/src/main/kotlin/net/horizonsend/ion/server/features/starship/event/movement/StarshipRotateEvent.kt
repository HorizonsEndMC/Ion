package net.horizonsend.ion.server.features.starship.event.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.movement.RotationMovement
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipRotateEvent(
	ship: ActiveControlledStarship,
	controller: Controller,
	override val movement: RotationMovement
) : StarshipMoveEvent(ship, controller, movement), Cancellable {
	val clockwise = movement.clockwise

	private var cancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	override fun isCancelled(): Boolean {
		return cancelled
	}

	override fun setCancelled(cancelled: Boolean) {
		this.cancelled = cancelled
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
