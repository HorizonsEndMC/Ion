package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.crafting.MultiblockRecipes
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

/**
 * Multiblock which processes multiblock recipes
 **/
interface RecipeEntity : ProgressMultiblock {
	var currentRecipe: MultiblockRecipe<*>

	fun fetchInventories()

	fun getRecipe(): MultiblockRecipe<*>? {
		return MultiblockRecipes.getRecipe(this as MultiblockEntity)
	}
}
