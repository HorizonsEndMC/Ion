package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment

/**
 * Represents a resource that is easily consumable from the enviornment, such as power
 **/
interface Consumable<T: Any?, E: RecipeEnviornment> : RecipeRequirement<T> {
	fun consume(enviornment: E)
}
