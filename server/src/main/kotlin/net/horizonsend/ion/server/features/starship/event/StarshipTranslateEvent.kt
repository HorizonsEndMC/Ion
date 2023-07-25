package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.movement.TranslateMovement
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipTranslateEvent(
	ship: ActivePlayerStarship,
	player: Player,
	override val movement: TranslateMovement
) : StarshipMoveEvent(ship, player, movement), Cancellable {
	val x = movement.dx
	val y = movement.dy
	val z = movement.dz

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
