package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

class FluidIngredient<T: MultiblockEntity>(val fluid: PipedFluid, val amount: Int) : MultiblockRecipeIngredient<T> {
	override fun checkRequirement(context: RecipeExecutionContext<T>): Boolean {
		val entity = context.entity

		if (entity !is FluidStoringEntity) return false

		return entity.capacities.any { it.internalStorage.getStoredFluid() == fluid && it.internalStorage.getAmount() >= amount }
	}

	override fun consumeIngredient(context: RecipeExecutionContext<T>): Boolean {
		val entity = context.entity

		if (entity !is FluidStoringEntity) return false

		return (entity.capacities.firstOrNull {
			it.internalStorage.getStoredFluid() == fluid && it.internalStorage.getAmount() >= amount
		}?.internalStorage?.remove(amount) ?: amount) == 0
	}
}
