package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.utilities.forbiddenCraftingItems
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class PrepareItemCraftListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
	fun onPrepareItemCraftEvent(event: PrepareItemCraftEvent) {
		if (forbiddenCraftingItems.contains(event.inventory.result?.type)) event.inventory.result = null
	}
}