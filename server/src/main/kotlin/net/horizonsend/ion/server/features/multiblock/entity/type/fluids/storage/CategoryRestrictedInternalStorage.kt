package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory

class CategoryRestrictedInternalStorage(
	private val storageCapacity: Int,
	private vararg val allowedCategories: FluidCategory
) : InternalStorage() {
	override fun getCapacity(): Int = storageCapacity

	override fun canStore(resource: PipedFluid, liters: Int): Boolean {
		if (liters + getAmount() > getCapacity()) {
			return false
		}

		// Check that the fluid attempting to be stored is the same as the one currently stored
		if (getStoredFluid() != null && resource != getStoredFluid()) {
			return false
		}

		return allowedCategories.intersect(resource.categories.toSet()).isNotEmpty()
	}
}
