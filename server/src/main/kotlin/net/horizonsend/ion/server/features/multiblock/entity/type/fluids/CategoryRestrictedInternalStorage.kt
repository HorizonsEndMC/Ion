package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FLUID_CATEGORIES
import org.bukkit.persistence.PersistentDataContainer

class CategoryRestrictedInternalStorage(
	private val storageCapacity: Int,
	private vararg val allowedCategories: FluidCategory
) : InternalStorage() {
	override val containerType: ContainerType = ContainerType.CATEGORY_RESTRICTED_INTERNAL_STORAGE

	override fun getCapacity(): Int = storageCapacity

	override fun canStore(resource: PipedFluid, liters: Double): Boolean {
		if (liters + getAmount() > getCapacity()) {
			return false
		}

		// Check that the fluid attempting to be stored is the same as the one currently stored
		if (getStoredFluid() != null && resource != getStoredFluid()) {
			return false
		}

		return allowedCategories.intersect(resource.categories.toSet()).isNotEmpty()
	}

	override fun saveAdditionalData(pdc: PersistentDataContainer) {
		pdc.set(FLUID_CATEGORIES, FluidCategory.listDataType, allowedCategories.toList())
	}
}
