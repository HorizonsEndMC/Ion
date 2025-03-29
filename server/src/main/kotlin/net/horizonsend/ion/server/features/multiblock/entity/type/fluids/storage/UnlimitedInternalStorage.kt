package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.core.registries.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType

/**
 * Internal storage with no limits on what fluid can be stored
 **/
class UnlimitedInternalStorage(private val storageCapacity: Int, override val inputAllowed: Boolean, override val extractionAllowed: Boolean) : InternalStorage() {
	override fun getCapacity(): Int = storageCapacity

	override fun canStore(fluid: FluidStack): Boolean {
		if (!canStore(fluid.type)) return false
		return fluid.amount + getAmount() <= getCapacity()
	}

	override fun canStore(type: FluidType): Boolean {
		if (type.key == FluidTypeKeys.EMPTY) return false // Cannot add an empty fluid stack
		if (type == getFluidType()) return true // If it is the same as the current fluid

		// Can only store if empty, since we know it isn't the current fluid
		return getFluidType().key == FluidTypeKeys.EMPTY
	}
}
