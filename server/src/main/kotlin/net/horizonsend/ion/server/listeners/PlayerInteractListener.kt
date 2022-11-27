package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.customitems.getCustomItem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		val item = event.item // If material is valid, then item is not null
		if (item != null) {
			item.getCustomItem().apply {
				when (event.action) {
					Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> this?.onPrimaryInteract(event.player, item)
					Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> this?.onSecondaryInteract(event.player, item)
					else -> return // Unknown Action Enum - We probably don't care, silently fail
				}
			}
			event.isCancelled = true
		}
	}
}