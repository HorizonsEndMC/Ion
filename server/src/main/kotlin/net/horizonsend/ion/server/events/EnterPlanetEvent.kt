package net.horizonsend.ion.server.events

import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class EnterPlanetEvent(val oldWorld: World, val newWorld: World, val controller: Controller?) : Event(true) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
