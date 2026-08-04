package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.crafting.input.FluidMultiblockEnviornment
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType

class FluidRecipeRequirement<E: FluidMultiblockEnviornment>(
	val storeName: String,
	val type: IonRegistryKey<FluidType, out FluidType>,
	val amount: Double
) : Consumable<FluidStack, E> {
	override fun consume(enviornment: E) {
		val storage = enviornment.fluidStore.getNamedStorage(storeName)
		storage?.removeAmount(amount)
	}

	override fun ensureAvailable(resource: FluidStack): Boolean {
		return resource.type == type && resource.amount >= amount
	}
}
