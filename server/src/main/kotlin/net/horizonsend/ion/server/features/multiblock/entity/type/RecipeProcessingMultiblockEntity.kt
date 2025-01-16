package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.newcrafting.MultiblockRecipeRegistry
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.NewMultiblockRecipe

interface RecipeProcessingMultiblockEntity<E: RecipeEnviornment> {
	fun buildRecipeEnviornment(): E

	fun getRecipesFor(): NewMultiblockRecipe<E>? {
		val enviornment = buildRecipeEnviornment()
		val recipes = MultiblockRecipeRegistry.getRecipesFor(this)
		val match = recipes.filter { recipe -> recipe.verifyAllRequirements(enviornment) }

		if (match.size > 1) IonServer.slF4JLogger.warn("Multiple recipes match input! This should not happen!!! Infringing recipes: ${match.joinToString { it.identifier }}")

		return match.firstOrNull()
	}

	fun tryProcessRecipe(): Boolean {
		val enviornment = buildRecipeEnviornment()
		val recipe = getRecipesFor()

		return recipe?.assemble(enviornment) != null
	}
}
