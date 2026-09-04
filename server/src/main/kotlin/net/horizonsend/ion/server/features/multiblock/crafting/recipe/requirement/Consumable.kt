package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnvironment

/**
 * Represents a resource that is easily consumable from the environment, such as power
 **/
interface Consumable<T: Any?, E: RecipeEnvironment> : RecipeRequirement<T> {
	fun consume(environment: E)
}
