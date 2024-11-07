package net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.result

import org.bukkit.inventory.ItemStack

class ItemResult(private val item: ItemStack) : MultiblockRecipeResult() {
	fun assemble(): ItemStack = item.clone()
}
