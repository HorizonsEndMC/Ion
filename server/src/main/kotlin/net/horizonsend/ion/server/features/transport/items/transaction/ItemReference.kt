package net.horizonsend.ion.server.features.transport.items.transaction

import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.ItemStack

data class ItemReference(
	val inventory: CraftInventory,
	val index: Int
) {
	fun get(): ItemStack? = inventory.getItem(index)
}
