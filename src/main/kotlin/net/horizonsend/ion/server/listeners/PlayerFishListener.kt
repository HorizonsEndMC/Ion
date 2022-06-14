package net.horizonsend.ion.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent

class PlayerFishListener : Listener {
	@EventHandler
	fun onPlayerFishEvent(event: PlayerFishEvent) {
		event.isCancelled = true
	}
}