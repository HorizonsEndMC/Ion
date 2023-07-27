package net.horizonsend.ion.server.features.starship.event.movement

import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.event.PlayerStarshipEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class StarshipStopCruisingEvent(
	ship: ActivePlayerStarship,
	val player: Player
) : PlayerStarshipEvent(ship) {
	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
