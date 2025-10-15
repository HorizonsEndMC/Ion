package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.GridEnergyEnviornment
import net.horizonsend.ion.server.features.multiblock.entity.type.gridenergy.GridEnergyMultiblock
import java.time.Duration

class GridEnergyRequirement<T: GridEnergyEnviornment>(val amount: Double, val threshold: Double = 1.0, val duration: Duration) : Consumable<Double, T> {
	override fun ensureAvailable(resource: Double): Boolean {
		return resource >= threshold
	}

	override fun consume(enviornment: T) {
		(enviornment.multiblock as GridEnergyMultiblock).setActiveDuration(duration)
		(enviornment.multiblock as GridEnergyMultiblock).setActiveGridEnergyConsumption(amount)
	}
}
