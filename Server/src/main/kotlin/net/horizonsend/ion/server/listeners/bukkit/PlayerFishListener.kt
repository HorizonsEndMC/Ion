package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent

@Suppress("Unused")
class PlayerFishListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerFishEvent(event: PlayerFishEvent) {
		event.isCancelled = true
	}
}