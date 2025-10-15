package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe

interface RecipeProcessingMultiblockEntity<E : RecipeEnviornment> {
	val recipeManager: MultiblockRecipeManager<E>

	class MultiblockRecipeManager<E : RecipeEnviornment>() {
		var lastRecipe: MultiblockRecipe<E>? = null
		var hasTicked: Boolean = false
		var lockedRecipe: IonRegistryKey<MultiblockRecipe<*>, MultiblockRecipe<E>>? = null
	}

	fun buildRecipeEnviornment(): E?

	fun getRecipesFor(): MultiblockRecipe<E>? {
		if (recipeManager.lockedRecipe != null) return recipeManager.lockedRecipe?.getValue()
		val enviornment = buildRecipeEnviornment() ?: return null
		// Optimization step, avoid checking all recipes
		if (recipeManager.lastRecipe?.verifyAllRequirements(enviornment, false) == true) return recipeManager.lastRecipe

		val recipes = IonRegistries.MULTIBLOCK_RECIPE.getRecipesFor(this)
		val match = recipes.filter { recipe -> recipe.verifyAllRequirements(enviornment, recipeManager.lockedRecipe != null) }

		if (match.size > 1) IonServer.slF4JLogger.warn("Multiple recipes match input! This should not happen!!! Infringing recipes: ${match.joinToString { it.key.toString() }}")

		return match.firstOrNull()
	}

	fun tryProcessRecipe(): Boolean {
		val recipe = getRecipesFor()

		if (this is ProgressMultiblock && (recipe == null || (recipeManager.lastRecipe != recipe && recipeManager.hasTicked))) progressManager.reset()
		val enviornment = buildRecipeEnviornment() ?: return false

		recipeManager.hasTicked = true
		recipeManager.lastRecipe = recipe

		return recipe?.assemble(enviornment) == true
	}
}
