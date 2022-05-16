package net.horizonsend.ion.miscellaneous.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent

internal class BlockFadeListener : Listener {
	@EventHandler
	fun onBlockFadeEvent(event: BlockFadeEvent) {
		if (event.block.type == Material.ICE) event.isCancelled = true
	}
}