package net.horizonsend.ion.server.features.qol

import net.starlegacy.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class RecipeListener : SLEventListener() {
	@EventHandler
	fun onClick(ev: InventoryClickEvent) {
		if (RecipeCommand.invs.contains(ev.view)) {
			ev.isCancelled = true
		}
	}
}
