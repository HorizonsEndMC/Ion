package net.horizonsend.ion.server.features.multiblock.recipe

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.recipe.ingredient.MultiblockRecipeIngredient
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import org.bukkit.block.Sign
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * A multiblock recipe that takes one ingredient, and produces one result
 * Usually furnace multiblocks with prismarine crystals occupying one slot
 *
 * @param time, in ticks
 * @param input, the input resource
 * @param result, the result of the recipe
 **/
class ProcessingMultiblockRecipe<T: Multiblock>(
	val time: Long,
	val input: MultiblockRecipeIngredient,
	override val multiblock: T,
	override val result: ItemStack
) : MultiblockRecipe<T> {
	init {
	    require(multiblock is FurnaceMultiblock)
	}

	override fun matches(multiblock: T, sign: Sign, inventory: Inventory): Boolean {
		return input.checkRequirement(multiblock, sign, inventory)
	}

	override fun execute(multiblock: T, sign: Sign, inventory: Inventory) {
		inventory as FurnaceInventory

		if (!LegacyItemUtils.canFit(inventory, result)) {
			return
		}

		// Return if enough ingredients are not present
		if (!checkAndConsume(input, multiblock, sign, inventory)) return

		inventory.result?.add(1) ?: { inventory.result = result }
	}
}
