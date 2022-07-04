package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("Unused")
	fun onPlayerJoinEvent(event: PlayerJoinEvent) {
		event.joinMessage(null)
	}
}