package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.fluids.TransportedFluids
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.FLUID_CATEGORIES
import net.horizonsend.ion.server.miscellaneous.utils.orEmpty
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

enum class ContainerType {
	UNLIMITED_INTERNAL_STORAGE {
		override fun create(primitive: PersistentDataContainer): InternalStorage {
			val capacity = primitive.get(NamespacedKeys.RESOURCE_CAPACITY_MAX, PersistentDataType.INTEGER)!!

			val storage =  UnlimitedInternalStorage(capacity)

			val fluid = primitive.get(NamespacedKeys.FLUID, PersistentDataType.STRING)?.let { TransportedFluids[it] }
			val amount = primitive.getOrDefault(NamespacedKeys.RESOURCE_AMOUNT, PersistentDataType.INTEGER, 0)

			storage.setFluid(fluid)
			storage.setAmount(amount)

			return storage
		}
	},
	CATEGORY_RESTRICTED_INTERNAL_STORAGE {
		override fun create(primitive: PersistentDataContainer): InternalStorage {
			val capacity = primitive.get(NamespacedKeys.RESOURCE_CAPACITY_MAX, PersistentDataType.INTEGER)!!
			val categories = primitive.get(FLUID_CATEGORIES, FluidCategory.listDataType).orEmpty().toTypedArray()

			val storage =  CategoryRestrictedInternalStorage(capacity, *categories)

			val fluid = primitive.get(NamespacedKeys.FLUID, PersistentDataType.STRING)?.let { TransportedFluids[it] }
			val amount = primitive.getOrDefault(NamespacedKeys.RESOURCE_AMOUNT, PersistentDataType.INTEGER, 0)

			storage.setFluid(fluid)
			storage.setAmount(amount)

			return storage
		}
	};

    abstract fun create(primitive: PersistentDataContainer): InternalStorage

    companion object {
        val persistentDataType = EnumDataType(ContainerType::class.java)
    }
}
