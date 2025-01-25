package net.horizonsend.ion.server.features.transport.filters

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer

interface FilterMeta {
	data class ItemFilterMeta(var filterMethod: FilterMethod = FilterMethod.LENIENT) : FilterMeta {
		companion object : PDCSerializers.RegisteredSerializer<ItemFilterMeta>("ITEM_FILTER_META", ItemFilterMeta::class) {
			val enumType = EnumDataType(FilterMethod::class.java)

			override fun toPrimitive(complex: ItemFilterMeta, context: PersistentDataAdapterContext, ): PersistentDataContainer {
				val pdc = context.newPersistentDataContainer()
				pdc.set(NamespacedKeys.SORTING_METHOD, enumType, complex.filterMethod)
				return pdc
			}

			override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext, ): ItemFilterMeta {
				return ItemFilterMeta(primitive.getOrDefault(NamespacedKeys.SORTING_METHOD, enumType, FilterMethod.LENIENT))
			}
		}
	}

	data object EmptyFilterMeta : PDCSerializers.RegisteredSerializer<EmptyFilterMeta>("EMPTY_META", EmptyFilterMeta::class), FilterMeta {
		override fun toPrimitive(complex: EmptyFilterMeta, context: PersistentDataAdapterContext, ): PersistentDataContainer = context.newPersistentDataContainer()
		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext, ): EmptyFilterMeta = EmptyFilterMeta
	}
}
