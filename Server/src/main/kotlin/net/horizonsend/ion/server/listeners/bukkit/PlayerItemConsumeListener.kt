package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

@Suppress("Unused")
class PlayerItemConsumeListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
		if (event.item.type != Material.POTION) return

		event.isCancelled = true
		event.setItem(null)
	}
}