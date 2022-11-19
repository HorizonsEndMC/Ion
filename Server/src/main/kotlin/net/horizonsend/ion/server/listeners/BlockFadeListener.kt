package net.horizonsend.ion.server.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent

class BlockFadeListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onBlockFadeEvent(event: BlockFadeEvent) {
		if (event.block.type != Material.ICE) return

		event.isCancelled = true
	}
}