package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidRegistry.EMPTY
import net.horizonsend.ion.server.features.transport.fluids.FluidStack

/**
 * Internal storage with no limits on what fluid can be stored
 **/
class UnlimitedInternalStorage(private val storageCapacity: Int, override val inputAllowed: Boolean, override val extractionAllowed: Boolean) : InternalStorage() {
	override fun getCapacity(): Int = storageCapacity

	override fun canStore(fluid: FluidStack): Boolean {
		if (!canStore(fluid.type)) return false
		return fluid.amount + getAmount() <= getCapacity()
	}

	override fun canStore(type: Fluid): Boolean {
		if (type == EMPTY) return false // Cannot add an empty fluid stack
		if (type == getFluidType()) return true // If it is the same as the current fluid

		// Can only store if empty, since we know it isn't the current fluid
		return getFluidType() == EMPTY
	}
}
