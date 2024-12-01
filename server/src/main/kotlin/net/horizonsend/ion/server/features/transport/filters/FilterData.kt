package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class FilterData<T : Any>(
	val isWhitelist: Boolean,
	val entries: MutableList<FilterKey<out T>>
) {
	companion object : PersistentDataType<PersistentDataContainer, FilterData<*>> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<FilterData<*>> = FilterData::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FilterData<*> {
			val whitelist = primitive.getOrDefault(NamespacedKeys.WHITELIST, PersistentDataType.BOOLEAN, true)
			val entries = primitive.getOrDefault(NamespacedKeys.FILTER_ENTRY, PersistentDataType.TAG_CONTAINER_ARRAY, arrayOf())
			val formatted = entries.mapTo(mutableListOf()) { entry ->
				FilterKey.fromPrimitive(entry, context)
			}

			return FilterData(whitelist, formatted)
		}

		override fun toPrimitive(complex: FilterData<*>, context: PersistentDataAdapterContext): PersistentDataContainer {
			val data = context.newPersistentDataContainer()

			data.set(NamespacedKeys.WHITELIST, PersistentDataType.BOOLEAN, complex.isWhitelist)
			val array = Array(complex.entries.size) { FilterKey.toPrimitive(complex.entries[it], context) }
			data.set(NamespacedKeys.FILTER_ENTRY, PersistentDataType.TAG_CONTAINER_ARRAY, array)

			return data
		}
	}
}
