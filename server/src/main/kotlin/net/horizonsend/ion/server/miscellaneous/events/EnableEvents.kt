package net.horizonsend.ion.server.miscellaneous.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class IonEnableEvent : Event() {
	private val handlers = HandlerList()
	override fun getHandlers(): HandlerList = handlers
}

class IonDisableEvent : Event() {
	private val handlers = HandlerList()
	override fun getHandlers(): HandlerList = handlers
}
