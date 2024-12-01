package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class FilterKey<T : Any>(val entry: T, val type: FilterType<out T>) {
	companion object : PersistentDataType<PersistentDataContainer, FilterKey<*>> {
		override fun getComplexType(): Class<FilterKey<*>> = FilterKey::class.java
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FilterKey<*> {
			val filterTypeIdentifier = primitive.get(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING)!!
			val filterType = FilterType.filterTypes[filterTypeIdentifier]!!

			return FilterKey(filterType.retrieve(primitive)!!, filterType)
		}

		override fun toPrimitive(complex: FilterKey<*>, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			val typeIdentifier = complex.type.javaClass.simpleName

			pdc.set(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING, typeIdentifier)
			complex.type.store(pdc, complex.entry)

			return pdc
		}
	}
}
