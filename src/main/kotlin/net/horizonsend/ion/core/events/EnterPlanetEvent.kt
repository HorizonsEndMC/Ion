package net.horizonsend.ion.core.events

import org.bukkit.World
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class EnterPlanetEvent(oldworld: World, newworld: World): Event() {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}