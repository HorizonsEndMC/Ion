package net.horizonsend.ion.server.features.qol

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class RecipeListener: Listener {
	@EventHandler
	fun onClick(ev: InventoryClickEvent) {
		if (ev.view.title == "Recipe")
			ev.isCancelled = true
	}
}
