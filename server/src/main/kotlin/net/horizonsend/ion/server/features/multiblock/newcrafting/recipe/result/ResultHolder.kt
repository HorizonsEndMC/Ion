package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.ItemResultEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment

abstract class ResultHolder<E: RecipeEnviornment, R: RecipeResult<E>>(val result: R) {
	fun verifySpace(input: E) = result.verifySpace(input)
	abstract fun execute(input: E)

	class ItemResultHolder<E: ItemResultEnviornment, R: ItemResult<E>>(result: R) : ResultHolder<E, R>(result) {
		override fun execute(enviornment: E) {
			val slotModificationWrapper = enviornment.getResultItemSlotModifier()
			result.execute(enviornment, slotModificationWrapper)
		}
	}

	companion object {
		fun <T: ItemResultEnviornment> of(result: ItemResult<T>) = ItemResultHolder(result)
	}
}
