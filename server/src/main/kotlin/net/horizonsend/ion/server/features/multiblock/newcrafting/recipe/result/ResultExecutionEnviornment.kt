package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.NewMultiblockRecipe
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result.RecipeExecutionResult.SuccessExecutionResult

class ResultExecutionEnviornment<E: RecipeEnviornment>(val parent: E, val recipe: NewMultiblockRecipe<E>) {
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
