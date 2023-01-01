package net.horizonsend.ion.server.legacy.events

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class BuySpawnShuttleEvent(val player: Player, val world: World) : Event() {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
