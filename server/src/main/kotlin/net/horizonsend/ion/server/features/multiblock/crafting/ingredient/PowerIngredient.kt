package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity

class PowerIngredient<T: MultiblockEntity>(val amount: Int) : MultiblockRecipeIngredient<T> {
	override fun checkRequirement(context: RecipeExecutionContext<T>): Boolean {
		val entity = context.entity

		if (entity !is PoweredMultiblockEntity) return false

		return entity.powerStorage.getPower() >= amount
	}

	override fun consumeIngredient(context: RecipeExecutionContext<T>): Boolean {
		val entity = context.entity

		if (entity !is PoweredMultiblockEntity) return false

		return entity.powerStorage.removePower(amount) == 0
	}
}
