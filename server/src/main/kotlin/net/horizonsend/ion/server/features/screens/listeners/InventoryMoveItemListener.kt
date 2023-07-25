package net.horizonsend.ion.server.features.screens.listeners

import net.horizonsend.ion.server.features.screens.ScreenManager.isScreen
import net.starlegacy.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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
