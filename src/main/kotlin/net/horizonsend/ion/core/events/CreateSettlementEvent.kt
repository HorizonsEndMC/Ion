package net.horizonsend.ion.core.events

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CreateSettlementEvent(val player: Player, val settlement: String) :Event() {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()

	}
}