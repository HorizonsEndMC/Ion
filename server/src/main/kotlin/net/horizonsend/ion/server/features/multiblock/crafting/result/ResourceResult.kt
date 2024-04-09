package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

class ResourceResult : MultiblockRecipeResult {
	override fun canFit(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign): Boolean {
		TODO("Not yet implemented")
	}

	override fun execute(recipe: MultiblockRecipe<*>, craftingInventory: Inventory, sign: Sign) {
		TODO("Not yet implemented")
	}
}
