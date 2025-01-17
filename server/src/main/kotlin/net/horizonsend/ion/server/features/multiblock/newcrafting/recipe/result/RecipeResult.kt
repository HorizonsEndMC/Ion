package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder

interface RecipeResult<E: RecipeEnviornment> {
	fun filterConsumedIngredients(enviornment: E, ingreidents: Collection<RequirementHolder<E, *, *>>): Collection<RequirementHolder<E, *, *>>

	fun verifySpace(enviornment: E): Boolean
}
