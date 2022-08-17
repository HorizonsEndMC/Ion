package net.horizonsend.ion.core.events

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class HyperspaceEnterEvent(val player: Player, val starship: ActiveStarship) : Event(true) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}