package net.horizonsend.ion.server.features.multiblock.crafting.input

import org.bukkit.inventory.ItemStack

interface ItemResultEnviornment : RecipeEnviornment {
	fun addItem(item: ItemStack)
	fun getResultSpaceFor(item: ItemStack): Int
}
