package net.horizonsend.ion.server.features.starship.event.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.event.ControlledStarshipEvent
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

abstract class StarshipMoveEvent(
	val ship: ActiveControlledStarship,
	val controller: Controller,
	open val movement: StarshipMovement
) : ControlledStarshipEvent(ship), Cancellable {
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
