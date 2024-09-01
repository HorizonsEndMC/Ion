package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

/**
 * Internal storage with no limits on what fluid can be stored
 **/
class UnlimitedInternalStorage(private val storageCapacity: Int) : InternalStorage() {
	override fun getCapacity(): Int = storageCapacity

	override fun canStore(resource: PipedFluid, liters: Int): Boolean {
		if (liters + getAmount() > getCapacity()) return false

		// Check that the fluid attempting to be stored is the same as the one currently stored
		return !(getStoredFluid() != null && resource != getStoredFluid())
	}
}
