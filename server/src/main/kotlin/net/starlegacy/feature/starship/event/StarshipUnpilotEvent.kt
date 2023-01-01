package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActivePlayerStarship
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipUnpilotEvent(
	ship: ActivePlayerStarship,
	val player: Player
) : PlayerStarshipEvent(ship), Cancellable {
	private var cancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	override fun isCancelled() = cancelled

	override fun setCancelled(cancelled: Boolean) {
		this.cancelled = cancelled
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
