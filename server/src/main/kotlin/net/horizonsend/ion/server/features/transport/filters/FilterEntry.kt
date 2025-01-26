package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class FilterEntry<T : Any, M : FilterMeta>(var value: T?, val type: FilterType<out T, out M>, val metaData: M) {
	fun matches(data: Any, isWhitelist: Boolean): Boolean {
		if (!type.typeClass.isInstance(data)) return false
		if (value == null) return !isWhitelist

		return type.castAndMatch(data, isWhitelist = isWhitelist, entry = this)
	}

	fun isEmpty() = value == null

	companion object : PersistentDataType<PersistentDataContainer, FilterEntry<*, *>> {
		override fun getComplexType(): Class<FilterEntry<*, *>> = FilterEntry::class.java
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FilterEntry<*, *> {
			val filterTypeIdentifier = primitive.get(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING)!!
			val filterType = FilterType[filterTypeIdentifier]

			val meta = primitive.get(NamespacedKeys.FILTER_META, PersistentDataType.TAG_CONTAINER)

			return FilterEntry(
				filterType.retrieveValue(primitive),
				filterType,
				meta?.let { filterType.retrieveMeta(meta, context) } ?: filterType.buildEmptyMeta()
			)
		}

		override fun toPrimitive(complex: FilterEntry<*, *>, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			val typeIdentifier = complex.type.identifier

			// Store filter type for meta and data retrieval
			pdc.set(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING, typeIdentifier)

			// Store value if present
			complex.type.store(pdc, complex.value)

			// Store meta data
			pdc.set(NamespacedKeys.FILTER_META, MetaDataContainer, PDCSerializers.pack(complex.metaData))

			return pdc
		}
	}
}
