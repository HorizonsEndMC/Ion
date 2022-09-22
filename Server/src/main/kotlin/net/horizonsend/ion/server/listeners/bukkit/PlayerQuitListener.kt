package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

@Suppress("Unused")
class PlayerQuitListener : Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	fun onPlayerQuitEvent(event: PlayerQuitEvent) {
		event.quitMessage(null)
	}
}