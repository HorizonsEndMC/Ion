package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class FilterEntry<T : Any>(var value: T?, val type: FilterType<out T>, /*TODO configuration*/) {
	companion object : PersistentDataType<PersistentDataContainer, FilterEntry<*>> {
		override fun getComplexType(): Class<FilterEntry<*>> = FilterEntry::class.java
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FilterEntry<*> {
			val filterTypeIdentifier = primitive.get(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING)!!
			val filterType = FilterType[filterTypeIdentifier]

			return FilterEntry(filterType.retrieve(primitive), filterType)
		}

		override fun toPrimitive(complex: FilterEntry<*>, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			val typeIdentifier = complex.type.javaClass.simpleName

			pdc.set(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING, typeIdentifier)
			complex.value?.let { complex.type.store(pdc, it) }

			return pdc
		}

		@Suppress("UNCHECKED_CAST")
		inline fun <reified T : Any> empty() = FilterEntry<T>(null, FilterType[T::class] as FilterType<T>)
	}
}
