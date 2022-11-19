package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.legacy.managers.ScreenManager.closeScreen
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryCloseEvent(event: InventoryCloseEvent) {
		(event.player as? Player)?.closeScreen()
	}
}