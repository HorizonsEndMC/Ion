package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

@Suppress("Unused")
class BlockPlaceListener : Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockPlaceEvent(event: BlockPlaceEvent) {
		val blockPlaced = event.blockPlaced
	}
}