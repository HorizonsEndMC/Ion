package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.ProgressEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import org.bukkit.inventory.ItemStack
import java.time.Duration

class WarmupResult<E : ProgressEnviornment>(val duration: Duration, val normalResult: ItemResult<E>) : ItemResult<E> {
	override fun verifySpace(enviornment: E): Boolean {
		return normalResult.verifySpace(enviornment)
	}

	override fun filterConsumedIngredients(
		enviornment: E,
		ingreidents: Collection<RequirementHolder<E, *, *>>
	): Collection<RequirementHolder<E, *, *>> {
		if (enviornment.getProgressManager().wouldComplete(duration)) return ingreidents
		return ingreidents.filterNot { holder -> holder.requirement is ItemRequirement }
	}

	override fun execute(enviornment: E, slotModificationWrapper: SlotModificationWrapper): RecipeExecutionResult {
		val progressManager = enviornment.getProgressManager()
		val complete = progressManager.addProgress(duration)

		if (!complete) return RecipeExecutionResult.ProgressExecutionResult(progressManager.getCurrentProgress())

		progressManager.reset()
		return normalResult.execute(enviornment, slotModificationWrapper)
	}

	override fun getResultItem(enviornment: E): ItemStack? {
		if (!enviornment.getProgressManager().wouldComplete(duration)) return null
		return normalResult.getResultItem(enviornment)
	}
}
