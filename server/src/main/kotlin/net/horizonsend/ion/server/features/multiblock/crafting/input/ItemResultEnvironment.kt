package net.horizonsend.ion.server.features.multiblock.crafting.input

import org.bukkit.inventory.ItemStack

interface ItemResultEnvironment : RecipeEnvironment {
	fun addItem(item: ItemStack)
	fun getResultSpaceFor(item: ItemStack): Int
}
