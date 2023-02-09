package net.horizonsend.ion.server.features.screens.listeners

import net.horizonsend.ion.server.features.screens.ScreenManager.isInScreen
import net.horizonsend.ion.server.features.screens.ScreenManager.screen
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if ((event.whoClicked as? Player)?.isInScreen == true) {
			(event.whoClicked as Player).screen!!.handleInventoryClick(event)
			event.isCancelled = true
		}
	}
}
