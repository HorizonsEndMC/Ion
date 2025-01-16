package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

interface RecipeRequirement<T : Any?> {
	fun ensureAvailable(resource: T): Boolean
}
