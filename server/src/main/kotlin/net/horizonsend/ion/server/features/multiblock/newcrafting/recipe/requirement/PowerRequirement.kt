package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.FurnaceEnviornment

class PowerRequirement(val amount: Int) : Consumable<Int, FurnaceEnviornment> {
	override fun ensureAvailable(resource: Int): Boolean {
		return resource >= amount
	}

	override fun consume(enviornment: FurnaceEnviornment) {
		enviornment.powerStorage.removePower(amount)
	}
}
