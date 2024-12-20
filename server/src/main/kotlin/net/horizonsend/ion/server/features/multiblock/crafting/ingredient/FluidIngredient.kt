package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.transport.fluids.Fluid

class FluidIngredient<T: MultiblockEntity>(val fluid: Fluid, val amount: Int) : MultiblockRecipeIngredient<T> {
	override fun checkRequirement(context: RecipeExecutionContext<T>): Boolean {
		val entity = context.entity

		if (entity !is FluidStoringEntity) return false

		return entity.fluidStores.any { it.internalStorage.getFluidType() == fluid && it.internalStorage.getAmount() >= amount }
	}

	override fun consumeIngredient(context: RecipeExecutionContext<T>): Boolean {
		val entity = context.entity

		if (entity !is FluidStoringEntity) return false

		return (entity.fluidStores.firstOrNull {
			it.internalStorage.getFluidType() == fluid && it.internalStorage.getAmount() >= amount
		}?.internalStorage?.removeAmount(amount) ?: amount) == 0
	}
}
