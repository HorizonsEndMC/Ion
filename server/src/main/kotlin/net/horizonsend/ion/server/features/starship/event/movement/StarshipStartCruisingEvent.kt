package net.horizonsend.ion.server.features.starship.event.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.event.ControlledStarshipEvent
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

class StarshipStartCruisingEvent(
    ship: ActiveControlledStarship,
    val controller: Controller
) : ControlledStarshipEvent(ship), Cancellable {
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
