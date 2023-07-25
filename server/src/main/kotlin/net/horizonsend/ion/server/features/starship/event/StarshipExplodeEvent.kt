package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipExplodeEvent(
	ship: ActiveStarship
) : StarshipEvent(ship), Cancellable {
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
