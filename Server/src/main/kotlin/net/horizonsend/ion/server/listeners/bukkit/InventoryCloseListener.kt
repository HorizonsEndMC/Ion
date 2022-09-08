package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.managers.ScreenManager.closeScreen
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

@BukkitListener
@Suppress("Unused")
class InventoryCloseListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryCloseEvent(event: InventoryCloseEvent) {
		(event.player as? Player)?.closeScreen()
	}
}