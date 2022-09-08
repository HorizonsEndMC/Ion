package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.managers.ScreenManager.isScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent

@BukkitListener
@Suppress("Unused")
class InventoryMoveItemListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryMoveItemEvent(event: InventoryMoveItemEvent) {
		if (event.initiator.isScreen) event.isCancelled = true
		if (event.destination.isScreen) event.isCancelled = true
		if (event.source.isScreen) event.isCancelled = true
	}
}