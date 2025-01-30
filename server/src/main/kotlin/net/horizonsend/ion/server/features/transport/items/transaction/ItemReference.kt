package net.horizonsend.ion.server.features.transport.items.transaction

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

data class ItemReference(
	val inventory: Inventory,
	val index: Int
) {
	fun get(): ItemStack? = inventory.getItem(index)
}
