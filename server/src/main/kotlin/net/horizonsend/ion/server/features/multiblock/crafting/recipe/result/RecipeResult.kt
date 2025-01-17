package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment

interface RecipeResult<E: RecipeEnviornment> {
	fun verifySpace(enviornment: E): Boolean
}
