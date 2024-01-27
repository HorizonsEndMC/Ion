package net.horizonsend.ion.server.features.starship.event

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class StarshipDetectedEvent(
	val player: Player,
	val world: World
) : Event(true) {
	private var cancelled: Boolean = false

	override fun getHandlers(): HandlerList = handlerList

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
