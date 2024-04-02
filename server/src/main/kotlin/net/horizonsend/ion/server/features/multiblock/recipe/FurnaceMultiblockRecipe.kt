package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.ResourceIngredient
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Recipes that use both slots of a furnace
 *
 * Do NOT try to use this on a non-furnace inventory
 **/
class FurnaceMultiblockRecipe<T: Multiblock>(
	val smelting: MultiblockRecipeIngredient,
	val fuel: MultiblockRecipeIngredient,
	private val resources: List<ResourceIngredient> = listOf(),
	override val multiblock: T,
	override val result: ItemStack
) : MultiblockRecipe<T> {
	init {
	    require(multiblock is FurnaceMultiblock)
	}

	override fun execute(multiblock: T, sign: Sign, inventory: Inventory) {
		inventory as FurnaceInventory

		if ((inventory.result?.amount ?: 0) >= result.maxStackSize) return

		// Return if enough ingredients are not present
		if (!checkAndConsume(smelting, multiblock, sign, inventory)) return
		if (!checkAndConsume(fuel, multiblock, sign, inventory)) return

		if (resources.any { !checkAndConsume(it, multiblock, sign, inventory) }) return

		inventory.result?.add(1) ?: run {
			inventory.result = result.asQuantity(1)
		}
	}

	override fun matches(multiblock: T, sign: Sign, inventory: Inventory): Boolean {
		inventory as FurnaceInventory

		if (resources.any { !it.checkRequirement(multiblock, sign, inventory) }) return false
		if (!smelting.checkRequirement(multiblock, sign, inventory)) return false
		if (!fuel.checkRequirement(multiblock, sign, inventory)) return false

		return true
	}
}
