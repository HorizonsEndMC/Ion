package net.horizonsend.ion.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class PlayerTeleportListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerTeleportEvent(event: PlayerTeleportEvent) {
		event.isCancelled = when (event.cause) {
			TeleportCause.CHORUS_FRUIT, TeleportCause.ENDER_PEARL -> true
			else -> false
		}
	}
}