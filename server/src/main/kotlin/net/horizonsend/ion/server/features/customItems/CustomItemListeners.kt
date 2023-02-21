package net.horizonsend.ion.server.features.customItems

import net.horizonsend.ion.server.features.customItems.CustomItems.customItem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class CustomItemListeners : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun rightClick(event: PlayerInteractEvent) {
		if (event.item == null) return

		val customItem = event.item?.customItem ?: return
		when (event.action) {
			Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
				customItem.handleSecondaryInteract(event.player, event.player.inventory.itemInMainHand)
				event.isCancelled = true
			}

			else -> return // Unknown Action Enum - We probably don't care, silently fail
		}
	}

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
