package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnvironment

interface RecipeResult<E: RecipeEnvironment> {
	fun verifySpace(environment: E): Boolean
}
