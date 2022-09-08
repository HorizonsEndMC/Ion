package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.annotations.BukkitListener
import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryDragEvent

@BukkitListener
@Suppress("Unused")
class InventoryDragListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	fun onInventoryDragEvent(event: InventoryDragEvent) {
		if ((event.whoClicked as? Player)?.isInScreen == true) event.isCancelled = true
	}
}