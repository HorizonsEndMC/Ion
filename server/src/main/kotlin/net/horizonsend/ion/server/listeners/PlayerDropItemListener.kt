package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.customitems.getCustomItem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerDropItemListener : Listener{
	@EventHandler
	@Suppress("unused")
	fun onPlayerDropItem(event: PlayerDropItemEvent) {
		if (event.itemDrop.itemStack.getCustomItem() != null) {

			event.itemDrop.itemStack.getCustomItem().apply {
				this?.onTertiaryInteract(event.player, event.itemDrop.itemStack)
			}

			event.isCancelled = true
		}
	}
}