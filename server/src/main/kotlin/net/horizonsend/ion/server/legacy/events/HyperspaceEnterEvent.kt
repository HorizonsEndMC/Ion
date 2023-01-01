package net.horizonsend.ion.server.legacy.events

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class HyperspaceEnterEvent(val player: Player, val starship: ActiveStarship) : Event() {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
