package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.MultiblockRecipeIngredient
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Recipes that use both slots of a furnace
 **/
class FurnaceMultiblockRecipe <T: Multiblock> (
	val time: Long,
	val smelting: MultiblockRecipeIngredient,
	val fuel: MultiblockRecipeIngredient,
	override val multiblock: T,
	override val result: ItemStack
) : MultiblockRecipe<T> {
	init {
	    require(multiblock is FurnaceMultiblock)
	}

	override fun execute(multiblock: T, sign: Sign, inventory: Inventory) {
		return //TODO
	}

	override fun matches(multiblock: T, sign: Sign, inventory: Inventory): Boolean {
		inventory as FurnaceInventory

		if (!smelting.checkRequirement(multiblock, sign, inventory)) return false
		if (!fuel.checkRequirement(multiblock, sign, inventory)) return false

		return true
	}
}
