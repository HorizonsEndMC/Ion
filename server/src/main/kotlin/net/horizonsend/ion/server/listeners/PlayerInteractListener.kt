package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.items.CustomItems.customItem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		if (event.item == null) return

		event.item?.customItem?.apply {
			when (event.action) {
				Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
					handleSecondaryInteract(event.player, event.player.inventory.itemInMainHand)
					event.isCancelled = true
				}
				else -> return // Unknown Action Enum - We probably don't care, silently fail
			}
		}
	}
}