package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

data class FilterData<T : Any, M : FilterMeta>(
	var position: BlockKey,
	val type: FilterType<out T, out M>,
	var entries: MutableList<out FilterEntry<out T, out M>> = mutableListOf(),
	var isWhitelist: Boolean = true,
) {
	companion object FilterDataSerializer : PDCSerializers.RegisteredSerializer<FilterData<*, *>>("FILTER_DATA", FilterData::class) {
		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FilterData<*, *> {
			val whitelist = primitive.getOrDefault(NamespacedKeys.WHITELIST, PersistentDataType.BOOLEAN, true)
			val entries = primitive.getOrDefault(NamespacedKeys.FILTER_ENTRY, PersistentDataType.LIST.dataContainers(), listOf())

			val filterTypeIdentifier = primitive.get(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING)!!

			val filterType = FilterType[filterTypeIdentifier]
			val formatted = filterType.loadFilterEntries(entries, context)

			return FilterData<Any, FilterMeta>(
				position = primitive.get(NamespacedKeys.BLOCK_KEY, PersistentDataType.LONG)!!,
				type = filterType,
				entries = formatted,
				isWhitelist = whitelist
			)
		}

		override fun toPrimitive(complex: FilterData<*, *>, context: PersistentDataAdapterContext): PersistentDataContainer {
			val data = context.newPersistentDataContainer()

			data.set(NamespacedKeys.BLOCK_KEY, PersistentDataType.LONG, complex.position)
			data.set(NamespacedKeys.WHITELIST, PersistentDataType.BOOLEAN, complex.isWhitelist)
			data.set(NamespacedKeys.FILTER_TYPE, PersistentDataType.STRING, complex.type.identifier)

			data.set(NamespacedKeys.FILTER_ENTRY, PersistentDataType.LIST.dataContainers(), complex.entries.map { key -> FilterEntry.toPrimitive(key, context) })

			return data
		}
	}
}
