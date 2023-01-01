package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.forbiddenCraftingItems
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class PrepareItemCraftListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPrepareItemCraftEvent(event: PrepareItemCraftEvent) {
		if (forbiddenCraftingItems.contains(event.inventory.result?.type)) event.inventory.result = null
	}
}
