package net.horizonsend.ion.server.features.screens.screens

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

abstract class Screen(val inventory: Inventory) {
	open fun handleInventoryClick(event: InventoryClickEvent) {}
}
