package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.customitems.getCustomItem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class PlayerItemSwapListener : Listener{
	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("unused")
	fun onPlayerSwapItem(event: PlayerSwapHandItemsEvent) {
		val item = event.offHandItem ?: return // The item swapped into the offhand

		val customItem = item.getCustomItem() ?: return

		event.isCancelled = true // Offhand item

		val newItem = event.player.inventory.itemInMainHand

		customItem.apply {
			this.onTertiaryInteract(event.player, newItem)
		}
	}
}