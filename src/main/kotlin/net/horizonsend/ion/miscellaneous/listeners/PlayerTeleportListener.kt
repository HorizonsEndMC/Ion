package net.horizonsend.ion.miscellaneous.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

internal class PlayerTeleportListener: Listener {
	@EventHandler
	fun onPlayerTeleportEvent(event: PlayerTeleportEvent) {
		event.isCancelled = when(event.cause) {
			TeleportCause.CHORUS_FRUIT -> true
			TeleportCause.ENDER_PEARL -> true
			else -> false
		}
	}
}