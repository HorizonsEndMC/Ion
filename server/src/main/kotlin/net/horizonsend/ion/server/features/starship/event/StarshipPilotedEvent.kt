package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class StarshipPilotedEvent(ship: ActiveControlledStarship, val player: Player) : ControlledStarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
