package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.starships.control.PlayerController
import net.starlegacy.feature.starship.active.ActiveStarships
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerMoveEvent(event: PlayerMoveEvent) {
		(ActiveStarships.findByPassenger(event.player)?.controller as? PlayerController)?.onPlayerMoveEvent(event)
	}
}
