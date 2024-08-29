package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

interface FluidStoringEntity {
	val capacities: Array<InternalStorage>

	fun canStore(fluid: PipedFluid, amount: Double) = capacities.any { it.canStore(fluid, amount) }

	fun firstCasStore(fluid: PipedFluid, amount: Double): InternalStorage? = capacities.firstOrNull { it.canStore(fluid, amount) }

	fun getStoredResources() : Map<PipedFluid?, Double> = capacities.associate { it.getStoredFluid() to it.getAmount().toDouble() }
}
