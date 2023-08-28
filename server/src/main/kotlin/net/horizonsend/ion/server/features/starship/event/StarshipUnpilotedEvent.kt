package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.event.HandlerList

class StarshipUnpilotedEvent(
	ship: ActiveControlledStarship,
	val controller: Controller
) : ControlledStarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
