package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

interface MultiblockRecipeIngredient<A: MultiblockEntity> {
	fun checkRequirement(context: RecipeExecutionContext<A>): Boolean
	fun consumeIngredient(context: RecipeExecutionContext<A>): Boolean
}
