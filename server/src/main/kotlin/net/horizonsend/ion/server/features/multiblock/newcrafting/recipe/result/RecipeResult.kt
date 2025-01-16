package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.MultiblockRecipeEnviornment

interface RecipeResult<E: MultiblockRecipeEnviornment> {
	fun verifySpace(input: E): Boolean
	fun execute(input: E)
}
