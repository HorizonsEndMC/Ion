package net.horizonsend.ion.server.features.multiblock.newcrafting.input

import org.bukkit.inventory.ItemStack

interface RecipeEnviornment {
	fun getItemSize(): Int
	fun getItem(index: Int): ItemStack?

	fun isEmpty(): Boolean
}
