package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipUnpilotEvent(
	ship: ActiveControlledStarship,
	val oldController: Controller,
	val newController: Controller,
	val cancellable: Boolean = false
) : ControlledStarshipEvent(ship), Cancellable {
	private var cancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	override fun isCancelled(): Boolean {
		return cancelled
	}

	override fun setCancelled(cancel: Boolean) {
		cancelled = if (!cancellable) false else cancel
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
