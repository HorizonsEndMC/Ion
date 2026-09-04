package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.crafting.input.ProgressEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import org.bukkit.inventory.ItemStack
import java.time.Duration

class WarmupResult<E : ProgressEnvironment>(val duration: Duration, val normalResult: ItemResult<E>) : ItemResult<E> {
	override fun asItem(): ItemStack = normalResult.asItem()

	override fun verifySpace(environment: E): Boolean {
		return normalResult.verifySpace(environment)
	}

	override fun buildTransaction(
		recipeEnvironment: E,
		resultEnvironment: ResultExecutionEnvironment<E>
	) {
		val progressManager = recipeEnvironment.getProgressManager()
		val complete = progressManager.addProgress(duration)

		if (!complete) {
			resultEnvironment.requirements.removeAll { holder ->
				holder.requirement is ItemRequirement
			}

			resultEnvironment.addResult { e ->
				RecipeExecutionResult.ProgressExecutionResult(progressManager.getCurrentProgress())
			}

			return
		}

		resultEnvironment.addResult { e ->
			progressManager.reset()
			RecipeExecutionResult.SuccessExecutionResult
		}

		normalResult.buildTransaction(recipeEnvironment, resultEnvironment)
	}

	override fun getResultItem(environment: E): ItemStack? {
		if (!environment.getProgressManager().wouldComplete(duration)) return null
		return normalResult.getResultItem(environment)
	}
}
