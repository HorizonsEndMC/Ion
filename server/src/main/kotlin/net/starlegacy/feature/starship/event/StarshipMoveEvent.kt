package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.movement.StarshipMovement
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

abstract class StarshipMoveEvent(
	val ship: ActiveStarship,
	val player: Player,
	open val movement: StarshipMovement
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
