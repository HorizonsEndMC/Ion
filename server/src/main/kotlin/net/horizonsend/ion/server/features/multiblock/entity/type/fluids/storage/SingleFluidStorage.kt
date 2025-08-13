package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType

class SingleFluidStorage(
	private val storageCapacity: Int,
	private val allowedFluid: FluidType,
	override val inputAllowed: Boolean,
	override val extractionAllowed: Boolean
) : InternalStorage() {
	override var fluidTypeUnsafe: FluidType = allowedFluid

	override fun getCapacity(): Int = storageCapacity

	override fun canStore(fluid: FluidStack): Boolean {
		if (!canStore(fluid.type)) return false
		return fluid.amount + getAmount() <= getCapacity()
	}

	override fun canStore(type: FluidType): Boolean {
		return type == allowedFluid
	}
}
