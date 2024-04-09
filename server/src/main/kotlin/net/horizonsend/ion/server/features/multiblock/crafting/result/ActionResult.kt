package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

@FunctionalInterface
interface ActionResult : MultiblockRecipeResult {
	override fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean {
		return true
	}
}
