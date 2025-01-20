package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidStack

class SingleFluidStorage(
	private val storageCapacity: Int,
	private val allowedFluid: Fluid,
	override val inputAllowed: Boolean,
	override val extractionAllowed: Boolean
) : InternalStorage() {
	override var fluidUnsafe: Fluid = allowedFluid

	override fun getCapacity(): Int = storageCapacity

	override fun canStore(fluid: FluidStack): Boolean {
		if (!canStore(fluid.type)) return false
		return fluid.amount + getAmount() <= getCapacity()
	}

	override fun canStore(type: Fluid): Boolean {
		return type == allowedFluid
	}
}
