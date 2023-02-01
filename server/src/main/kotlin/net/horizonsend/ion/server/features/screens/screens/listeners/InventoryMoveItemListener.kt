package net.horizonsend.ion.server.features.screens.screens.listeners

import net.horizonsend.ion.server.features.screens.screens.ScreenManager.isScreen
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryMoveItemEvent

class InventoryMoveItemListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryMoveItemEvent(event: InventoryMoveItemEvent) {
		if (event.initiator.isScreen) event.isCancelled = true
		if (event.destination.isScreen) event.isCancelled = true
		if (event.source.isScreen) event.isCancelled = true
	}
}
