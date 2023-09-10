package net.horizonsend.ion.server.features.screens.listeners

import net.horizonsend.ion.server.features.screens.ScreenManager.isInScreen
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryDragEvent

class InventoryDragListener : SLEventListener() {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryDragEvent(event: InventoryDragEvent) {
		if ((event.whoClicked as? Player)?.isInScreen == true) event.isCancelled = true
	}
}
