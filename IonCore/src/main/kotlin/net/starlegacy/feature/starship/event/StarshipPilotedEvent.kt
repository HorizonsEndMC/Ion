package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActivePlayerStarship
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class StarshipPilotedEvent(ship: ActivePlayerStarship, val player: Player) : PlayerStarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
