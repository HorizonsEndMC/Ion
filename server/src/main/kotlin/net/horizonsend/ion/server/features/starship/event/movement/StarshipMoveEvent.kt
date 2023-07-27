package net.horizonsend.ion.server.features.starship.event.movement

import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.event.PlayerStarshipEvent
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

abstract class StarshipMoveEvent(
	val ship: ActivePlayerStarship,
	val player: Player,
	open val movement: StarshipMovement
) : PlayerStarshipEvent(ship), Cancellable {
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
