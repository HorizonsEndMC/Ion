package net.horizonsend.ion.server.features.transport.filters

import com.manya.pdc.minecraft.ItemStackDataType
import net.horizonsend.ion.server.features.transport.fluids.Fluid
import net.horizonsend.ion.server.features.transport.fluids.FluidPersistentDataType
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

abstract class FilterType<T : Any>(val cacheType: CacheType, val typeClass: Class<T>, val persistentDataType: PersistentDataType<*, T>) {
	data object FluidType : FilterType<Fluid>(CacheType.FLUID, Fluid::class.java, FluidPersistentDataType)
	data object ItemType : FilterType<ItemStack>(CacheType.ITEMS, ItemStack::class.java, ItemStackDataType())

	fun store(pdc: PersistentDataContainer, data: Any) {
		if (!typeClass.isInstance(data)) return
		@Suppress("UNCHECKED_CAST")
		pdc.set(NamespacedKeys.FILTER_ENTRY, persistentDataType, data as T)
	}

	fun retrieve(pdc: PersistentDataContainer): T? {
		return pdc.get(NamespacedKeys.FILTER_ENTRY, persistentDataType)
	}

	companion object {
		val filterTypes = mutableMapOf<String, FilterType<*>>(
			FluidType.javaClass.simpleName to FluidType,
			ItemType.javaClass.simpleName to ItemType,
		)
	}
}
