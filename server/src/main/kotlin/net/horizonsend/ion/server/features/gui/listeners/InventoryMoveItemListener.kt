package net.horizonsend.ion.server.features.gui.listeners

import net.horizonsend.ion.server.features.gui.ScreenManager.isScreen
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryMoveItemEvent

class InventoryMoveItemListener : SLEventListener() {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryMoveItemEvent(event: InventoryMoveItemEvent) {
		if (event.initiator.isScreen) event.isCancelled = true
		if (event.destination.isScreen) event.isCancelled = true
		if (event.source.isScreen) event.isCancelled = true
	}
}
