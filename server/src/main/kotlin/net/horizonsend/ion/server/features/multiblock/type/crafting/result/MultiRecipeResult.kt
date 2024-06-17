package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

/**
 * Wrap multiple result actions
 **/
class MultiRecipeResult(
	val main: MultiblockRecipeResult,
	vararg others: ActionResult
) : MultiblockRecipeResult {
	val actions = others.toList()

	override fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean {
		return main.canFit(recipe, craftingInventory, sign)
	}

	override fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign) {
		main.execute(recipe, craftingInventory, sign)
		actions.forEach { it.execute(recipe, craftingInventory, sign) }
	}
}
