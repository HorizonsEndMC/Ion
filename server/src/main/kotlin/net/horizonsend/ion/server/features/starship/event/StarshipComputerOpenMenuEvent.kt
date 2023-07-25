package net.horizonsend.ion.server.features.starship.event

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class StarshipComputerOpenMenuEvent(
	val player: Player
) : Event(), Cancellable {
	private var cancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	override fun isCancelled(): Boolean {
		return cancelled
	}

	override fun setCancelled(cancelled: Boolean) {
		this.cancelled = cancelled
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
