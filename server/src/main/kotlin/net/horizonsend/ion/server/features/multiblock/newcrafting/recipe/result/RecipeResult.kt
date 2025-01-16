package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment

interface RecipeResult<E: RecipeEnviornment> {
	fun verifySpace(input: E): Boolean
	fun execute(input: E)
}
