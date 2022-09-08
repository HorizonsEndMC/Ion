package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.annotations.BukkitListener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

@BukkitListener
@Suppress("Unused")
class PlayerTeleportListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerTeleportEvent(event: PlayerTeleportEvent) {
		event.isCancelled = when (event.cause) {
			TeleportCause.CHORUS_FRUIT, TeleportCause.ENDER_PEARL -> true
			else -> false
		}
	}
}