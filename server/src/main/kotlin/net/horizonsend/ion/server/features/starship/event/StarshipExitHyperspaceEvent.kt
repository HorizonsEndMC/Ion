package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import org.bukkit.event.HandlerList

class StarshipExitHyperspaceEvent(ship: ActiveStarship, val movement: HyperspaceMovement) : StarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
