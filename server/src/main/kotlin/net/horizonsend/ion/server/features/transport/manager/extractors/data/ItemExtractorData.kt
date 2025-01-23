package net.horizonsend.ion.server.features.transport.manager.extractors.data

import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class ItemExtractorData(pos: BlockKey, metaData: ItemExtractorMetaData) : AdvancedExtractorData<ItemExtractorData.ItemExtractorMetaData>(pos, metaData) {
	override val metaSerializer: PDCSerializers.RegisteredSerializer<ItemExtractorMetaData> = PDCSerializers.ITEM_EXTRACTOR_METADATA

	data class ItemExtractorMetaData(
		override val key: BlockKey,
		var sortingOrder: SortingOrder = SortingOrder.NEAREST_FIRST
	) : ExtractorMetaData {
		var roundRobinIndex = 0

		override fun toExtractorData(): ExtractorData {
			return ItemExtractorData(key, this)
		}

		companion object : PDCSerializers.RegisteredSerializer<ItemExtractorMetaData>("ITEM_EXTRACTOR_METADATA", ItemExtractorMetaData::class) {
			override fun toPrimitive(
				complex: ItemExtractorMetaData,
				context: PersistentDataAdapterContext,
			): PersistentDataContainer {
				val pdc = context.newPersistentDataContainer()
				pdc.set(NamespacedKeys.BLOCK_KEY, PersistentDataType.LONG, complex.key)
				pdc.set(NamespacedKeys.SORTING_ORDER, SortingOrder.serializationType, complex.sortingOrder)
				return pdc
			}

			override fun fromPrimitive(
				primitive: PersistentDataContainer,
				context: PersistentDataAdapterContext,
			): ItemExtractorMetaData {
				return ItemExtractorMetaData(
					primitive.get(NamespacedKeys.BLOCK_KEY, PersistentDataType.LONG)!!,
					primitive.getOrDefault(NamespacedKeys.SORTING_ORDER, SortingOrder.serializationType, SortingOrder.NEAREST_FIRST)
				)
			}
		}
	}
}
