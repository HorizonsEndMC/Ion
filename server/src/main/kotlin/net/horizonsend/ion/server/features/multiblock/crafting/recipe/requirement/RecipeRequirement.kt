package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

interface RecipeRequirement<T : Any?> {
	fun ensureAvailable(resource: T): Boolean
}
