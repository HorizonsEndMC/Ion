package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

interface MultiblockRecipeResult<T: MultiblockEntity> {
	fun canFit(context: RecipeExecutionContext<T>): Boolean
	fun execute(context: RecipeExecutionContext<T>)
}
