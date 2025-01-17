package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.MultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.result.RecipeExecutionResult.SuccessExecutionResult

class ResultExecutionEnviornment<E: RecipeEnviornment>(val parent: E, val recipe: MultiblockRecipe<E>) {
	val requirements = recipe.getAllRequirements().toMutableList()

	private val results: MutableList<(E) -> RecipeExecutionResult> = mutableListOf()

	fun addResult(result: (E) -> RecipeExecutionResult) {
		results.add(result)
	}

	fun executeResult(): RecipeExecutionResult {
		var result: RecipeExecutionResult = SuccessExecutionResult

		results.forEach { t ->
			result = t.invoke(parent)
		}

		return result
	}
}
