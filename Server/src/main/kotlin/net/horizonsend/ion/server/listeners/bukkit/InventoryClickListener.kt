package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import net.horizonsend.ion.server.managers.ScreenManager.screen
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

@Suppress("Unused")
class InventoryClickListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if ((event.whoClicked as? Player)?.isInScreen == true) {
			(event.whoClicked as Player).screen!!.handleInventoryClick(event)
			event.isCancelled = true
		}
	}
}