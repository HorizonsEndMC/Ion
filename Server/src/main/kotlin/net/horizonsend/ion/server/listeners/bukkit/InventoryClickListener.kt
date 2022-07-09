package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.managers.ScreenManager.isInScreen
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
	fun onInventoryClickEvent(event: InventoryClickEvent) {
		if (event.whoClicked.isInScreen) event.isCancelled = true

		// Skip if null
		if (event.currentItem == null) return
		if (event.cursor == null) return

		// Skip if not custom item material
		if (event.currentItem!!.type != Material.WARPED_FUNGUS_ON_A_STICK) return
		if (event.cursor!!.type != Material.WARPED_FUNGUS_ON_A_STICK) return

		// Skip if not custom items
		if (!event.currentItem!!.itemMeta.hasCustomModelData()) return
		if (!event.cursor!!.itemMeta.hasCustomModelData()) return

		// Skip if not same custom item
		if (event.currentItem!!.itemMeta.customModelData != event.cursor!!.itemMeta.customModelData) return

		val items = IonServer.customItems[event.currentItem!!.itemMeta.customModelData]!!
			.combine(Pair(event.currentItem!!, event.cursor!!))

		event.currentItem = items.first

		// InventoryClickEvent#setCursor is deprecated, annoying work around. :/
		event.whoClicked.setItemOnCursor(items.second)

		event.isCancelled = true
	}
}