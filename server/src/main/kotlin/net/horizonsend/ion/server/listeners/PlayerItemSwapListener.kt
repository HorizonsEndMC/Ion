package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.items.CustomItems.customItem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class PlayerItemSwapListener : Listener{
	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("unused")
	fun onPlayerSwapItem(event: PlayerSwapHandItemsEvent) {
		// We have to get it from the inventory and not the event, otherwise things break
		val itemStack = event.player.inventory.itemInMainHand

		val customItem = itemStack.customItem ?: return

		event.isCancelled = true

		customItem.handleTertiaryInteract(event.player, itemStack)
	}
}