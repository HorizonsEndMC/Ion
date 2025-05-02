package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.kyori.adventure.text.Component

sealed interface RecipeExecutionResult {
	sealed interface Success : RecipeExecutionResult

	data object SuccessExecutionResult : Success
	data class FailureExecutionResult(val reason: Component) : Success
	data class ProgressExecutionResult(val newProgress: Double) : Success
}
