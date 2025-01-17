package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.crafting.input.ProgressEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import org.bukkit.inventory.ItemStack
import java.time.Duration

class WarmupResult<E : ProgressEnviornment>(val duration: Duration, val normalResult: ItemResult<E>) : ItemResult<E> {
	override fun verifySpace(enviornment: E): Boolean {
		return normalResult.verifySpace(enviornment)
	}

	override fun buildTransaction(
		recipeEnviornment: E,
		resultEnviornment: ResultExecutionEnviornment<E>,
		slotModificationWrapper: SlotModificationWrapper
	) {
		val progressManager = recipeEnviornment.getProgressManager()
		val complete = progressManager.addProgress(duration)

		if (!complete) {
			resultEnviornment.requirements.removeAll { holder ->
				holder.requirement is ItemRequirement
			}

			resultEnviornment.addResult { e ->
				RecipeExecutionResult.ProgressExecutionResult(progressManager.getCurrentProgress())
			}

			return
		}

		resultEnviornment.addResult { e ->
			progressManager.reset()
			RecipeExecutionResult.SuccessExecutionResult
		}

		normalResult.buildTransaction(recipeEnviornment, resultEnviornment, slotModificationWrapper)
	}

	override fun getResultItem(enviornment: E): ItemStack? {
		if (!enviornment.getProgressManager().wouldComplete(duration)) return null
		return normalResult.getResultItem(enviornment)
	}
}
