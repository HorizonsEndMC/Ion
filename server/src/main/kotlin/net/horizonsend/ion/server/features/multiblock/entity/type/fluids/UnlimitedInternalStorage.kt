package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid

/**
 * Internal storage with no limits on what fluid can be stored
 **/
class UnlimitedInternalStorage(private val storageCapacity: Int) : InternalStorage() {
	override val containerType: ContainerType = ContainerType.UNLIMITED_INTERNAL_STORAGE

	override fun getCapacity(): Int = storageCapacity

	override fun canStore(resource: PipedFluid, liters: Double): Boolean {
		if (liters + getAmount() > getCapacity()) return false

		// Check that the fluid attempting to be stored is the same as the one currently stored
		return !(getStoredFluid() != null && resource != getStoredFluid())
	}
}
