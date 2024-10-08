package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import org.bukkit.event.HandlerList

class StarshipUnpilotedEvent(
	ship: ActiveControlledStarship,
	val oldController: Controller,
	val newController: Controller
) : ControlledStarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
