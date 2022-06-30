package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.managers.ScreenManager.closeScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
	fun onInventoryCloseEvent(event: InventoryCloseEvent) {
		event.player.closeScreen()
	}
}