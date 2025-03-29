package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory

class CategoryRestrictedInternalStorage(
	private val storageCapacity: Int,
	override val inputAllowed: Boolean,
	override val extractionAllowed: Boolean,
	private vararg val allowedCategories: FluidCategory,
) : InternalStorage() {
	override fun getCapacity(): Int = storageCapacity

	override fun canStore(fluid: FluidStack): Boolean {
		if (!canStore(fluid.type)) return false
		return fluid.amount + getAmount() <= getCapacity()
	}

	override fun canStore(type: FluidType): Boolean {
		if (type.key == FluidTypeKeys.EMPTY) return false // Cannot add an empty fluid stack
		if (type == getFluidType()) return true // If it is the same as the current fluid

		return if (getFluidType().key == FluidTypeKeys.EMPTY) { // If this is currently empty
			allowedCategories.intersect(type.categories.toSet()).isNotEmpty() // Check the category restriction
 		} else false // Already know its not the stored fluid, so can't store
	}
}
