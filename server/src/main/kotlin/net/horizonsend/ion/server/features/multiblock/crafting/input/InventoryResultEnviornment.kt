package net.horizonsend.ion.server.features.multiblock.crafting.input

import net.horizonsend.ion.server.features.transport.items.util.getTransferSpaceFor
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface InventoryResultEnviornment: RecipeEnviornment, ItemResultEnviornment {
	fun getResultInventory(): Inventory

	override fun addItem(item: ItemStack) {
		getResultInventory().addItem(item)
	}

	override fun getResultSpaceFor(item: ItemStack): Int {
		return getTransferSpaceFor(getResultInventory() as CraftInventory, item)
	}
}
