package net.horizonsend.ion.server.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class PlayerItemConsumeListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
		if (event.item.type != Material.POTION) return

		event.isCancelled = true
		event.setItem(null)
	}
}
