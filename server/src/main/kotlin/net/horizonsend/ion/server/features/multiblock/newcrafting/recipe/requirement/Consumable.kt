package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment

/**
 * Represents a resource that is easily consumable from the enviornment, such as power
 **/
interface Consumable<T: Any?, E: RecipeEnviornment> : RecipeRequirement<T> {
	fun consume(enviornment: E)
}
