package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

/**
 * Wrap multiple result actions
 **/
class MultiRecipeResult(
	vararg val recipes: MultiblockRecipeResult
) : MultiblockRecipeResult {
	override fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean {
		return recipes.all { it.canFit(recipe, craftingInventory, sign) }
	}

	override fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign) {
		recipes.forEach { it.execute(recipe, craftingInventory, sign) }
	}
}
