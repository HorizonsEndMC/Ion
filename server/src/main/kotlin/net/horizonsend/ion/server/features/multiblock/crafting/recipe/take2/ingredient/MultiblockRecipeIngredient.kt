package net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.NewRecipe

abstract class MultiblockRecipeIngredient {
	abstract fun consume(context: NewRecipe.ExecutionContext)
	abstract fun check(context: NewRecipe.ExecutionContext): Boolean
}
