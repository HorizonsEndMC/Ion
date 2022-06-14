package net.horizonsend.ion.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {
	@EventHandler
	fun onPlayerQuitEvent(event: PlayerQuitEvent) {
		event.quitMessage(null)
	}
}