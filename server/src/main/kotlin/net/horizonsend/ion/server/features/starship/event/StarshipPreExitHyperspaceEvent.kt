package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Location
import org.bukkit.event.HandlerList

class StarshipPreExitHyperspaceEvent(ship: ActiveStarship, val successful: Boolean, var exitLocation: Location) : StarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
