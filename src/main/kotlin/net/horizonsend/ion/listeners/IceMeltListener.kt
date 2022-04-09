package net.horizonsend.ion.listeners

import org.bukkit.Material.ICE
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent

internal class IceMeltListener: Listener {
	@EventHandler
	fun onIceMelt(event: BlockFadeEvent) {
		if (event.block.type == ICE) event.isCancelled = true
	}
}