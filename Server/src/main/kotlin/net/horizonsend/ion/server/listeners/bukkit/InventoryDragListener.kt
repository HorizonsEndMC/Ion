package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryDragEvent

@Suppress("Unused")
class InventoryDragListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryDragEvent(event: InventoryDragEvent) {
		if (event.whoClicked.isInScreen) event.isCancelled = true
	}
}