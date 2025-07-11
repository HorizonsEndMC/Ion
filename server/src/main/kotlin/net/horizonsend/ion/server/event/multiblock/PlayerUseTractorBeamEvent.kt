package net.horizonsend.ion.server.event.multiblock

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerUseTractorBeamEvent(val player: Player, val destination: Location) : Event(/* isAsync = */ false), Cancellable {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	private var cancelled: Boolean = false

	override fun setCancelled(cancel: Boolean) {
		cancelled = cancel
	}

	override fun isCancelled(): Boolean {
		return cancelled
	}
}
