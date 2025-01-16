package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.MultiblockRecipeEnviornment

interface RecipeResult<Input: MultiblockRecipeEnviornment> {
	fun verifySpace(input: Input)
	fun execute(input: Input)
}
