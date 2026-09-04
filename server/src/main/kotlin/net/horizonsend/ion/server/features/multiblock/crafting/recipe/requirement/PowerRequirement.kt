package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.PoweredEnvironment

class PowerRequirement<T: PoweredEnvironment>(val amount: Int) : Consumable<Int, T> {
	override fun ensureAvailable(resource: Int): Boolean {
		return resource >= amount
	}

	override fun consume(environment: T) {
		environment.powerStorage.removePower(amount)
	}
}
