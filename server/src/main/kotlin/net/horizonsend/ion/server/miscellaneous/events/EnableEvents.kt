package net.horizonsend.ion.server.miscellaneous.events

import co.aikar.commands.PaperCommandManager
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class IonEnableEvent(val manager: PaperCommandManager) : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}

class IonDisableEvent : Event() {
	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun getHandlers(): HandlerList = handlerList
}
