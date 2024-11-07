package net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.ingredient

import net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2.NewRecipe
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity

class PowerIngredient(private val amount: Int) : ResourceIngredient() {
	override fun check(context: NewRecipe.ExecutionContext): Boolean {
		return context.entity is PoweredMultiblockEntity && context.entity.powerStorage.canRemovePower(amount)
	}

	override fun consume(context: NewRecipe.ExecutionContext) {
		(context.entity as PoweredMultiblockEntity).powerStorage.removePower(amount)
	}
}
