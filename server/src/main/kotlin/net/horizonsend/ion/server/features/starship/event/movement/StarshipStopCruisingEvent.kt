package net.horizonsend.ion.server.features.starship.event.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.event.ControlledStarshipEvent
import org.bukkit.event.HandlerList

class StarshipStopCruisingEvent(
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
