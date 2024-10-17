package net.horizonsend.ion.server.features.multiblock.crafting.result

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

fun interface ActionResult<T: MultiblockEntity> : MultiblockRecipeResult<T> {
	override fun canFit(context: RecipeExecutionContext<T>): Boolean {
		return true
	}
}
