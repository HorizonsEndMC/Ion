package net.horizonsend.ion.miscellaneous.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
	@EventHandler
	fun onPlayerJoinEvent(event: PlayerJoinEvent) = event.joinMessage(null)
}