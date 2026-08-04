package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.PoweredEnviornment

class PowerRequirement<T: PoweredEnviornment>(val amount: Int) : Consumable<Int, T> {
	override fun ensureAvailable(resource: Int): Boolean {
		return resource >= amount
	}

	override fun consume(enviornment: T) {
		enviornment.powerStorage.removePower(amount)
	}
}
