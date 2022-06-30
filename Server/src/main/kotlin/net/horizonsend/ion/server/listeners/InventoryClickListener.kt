package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if (event.whoClicked.isInScreen) event.isCancelled = true
	}
}