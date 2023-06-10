package net.horizonsend.ion.server.miscellaneous.events

import co.aikar.commands.PaperCommandManager
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class IonEnableEvent(val manager: PaperCommandManager) : Event() {
	private val handlers = HandlerList()
	override fun getHandlers(): HandlerList = handlers
}

class IonDisableEvent : Event() {
	private val handlers = HandlerList()
	override fun getHandlers(): HandlerList = handlers
}
