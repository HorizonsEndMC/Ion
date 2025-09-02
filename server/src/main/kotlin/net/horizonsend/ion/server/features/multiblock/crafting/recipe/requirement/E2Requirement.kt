package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.E2Enviornment
import net.horizonsend.ion.server.features.multiblock.entity.type.e2.E2Multiblock
import java.time.Duration

class E2Requirement<T: E2Enviornment>(val amount: Double, val threshold: Double = 1.0, val duration: Duration) : Consumable<Double, T> {
	override fun ensureAvailable(resource: Double): Boolean {
		return resource >= threshold
	}

	override fun consume(enviornment: T) {
		(enviornment.multiblock as E2Multiblock).setActiveDuration(duration)
		(enviornment.multiblock as E2Multiblock).setActiveE2Consumption(amount)
	}
}
