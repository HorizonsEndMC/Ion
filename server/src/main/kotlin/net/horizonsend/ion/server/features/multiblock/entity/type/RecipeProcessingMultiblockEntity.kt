package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe

interface RecipeProcessingMultiblockEntity<E: RecipeEnviornment> {
	var lastRecipe: MultiblockRecipe<E>?

	fun buildRecipeEnviornment(): E

	fun getRecipesFor(): MultiblockRecipe<E>? {
		val enviornment = buildRecipeEnviornment()
		// Optimization step, avoid checking all recipes
		if (lastRecipe?.verifyAllRequirements(enviornment) == true) return lastRecipe

		val recipes = IonRegistries.MULTIBLOCK_RECIPE.getRecipesFor(this)
		val match = recipes.filter { recipe -> recipe.verifyAllRequirements(enviornment) }

		if (match.size > 1) IonServer.slF4JLogger.warn("Multiple recipes match input! This should not happen!!! Infringing recipes: ${match.joinToString { it.key.toString() }}")

		return match.firstOrNull()
	}

	fun tryProcessRecipe(): Boolean {
		val recipe = getRecipesFor()

		if (this is ProgressMultiblock && (recipe == null || lastRecipe != recipe)) progressManager.reset()

		lastRecipe = recipe

		val enviornment = buildRecipeEnviornment()
		return recipe?.assemble(enviornment) != null
	}
}
