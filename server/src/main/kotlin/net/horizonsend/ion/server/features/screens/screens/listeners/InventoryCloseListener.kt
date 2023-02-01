package net.horizonsend.ion.server.features.screens.screens.listeners

import net.horizonsend.ion.server.features.screens.screens.ScreenManager.closeScreen
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryCloseEvent(event: InventoryCloseEvent) {
		(event.player as? Player)?.closeScreen()
	}
}
