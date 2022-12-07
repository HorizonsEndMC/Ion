package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.customitems.getCustomItem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class PlayerItemSwapListener : Listener{
	@EventHandler
	@Suppress("unused")
	fun onPlayerDropItem(event: PlayerSwapHandItemsEvent){
		event.isCancelled = true
		if (event.mainHandItem?.getCustomItem() != null){
			event.mainHandItem?.getCustomItem().apply {
				this?.onPrimaryInteract(event.player, event.mainHandItem!!)
			}
		}
	}
}