package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.event.HandlerList

class StarshipDeactivatedEvent(ship: ActiveStarship) : StarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
