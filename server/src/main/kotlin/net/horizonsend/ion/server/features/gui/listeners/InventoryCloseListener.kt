package net.horizonsend.ion.server.features.gui.listeners

import net.horizonsend.ion.server.features.gui.ScreenManager.closeScreen
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener : SLEventListener() {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryCloseEvent(event: InventoryCloseEvent) {
		(event.player as? Player)?.closeScreen()
	}
}
