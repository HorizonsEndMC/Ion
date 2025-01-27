package net.horizonsend.ion.server.features.transport.items

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

enum class SortingOrder {
	NEAREST_FIRST {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			val extractorPosition = toVec3i(extractorData.key)
			return destinations.minBy { key -> extractorPosition.distance(toVec3i(key)) }
		}
	},
	ROUND_ROBIN {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			val currentIndex = extractorData.roundRobinIndex.toInt()

			val nextIndex = (currentIndex + 1) % destinations.size
			extractorData.roundRobinIndex = nextIndex.toUShort()
			return destinations[nextIndex]
		}
	},
	FARTHEST_FIRST {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			val extractorPosition = toVec3i(extractorData.key)
			return destinations.maxBy { key -> extractorPosition.distance(toVec3i(key)) }
		}
	},
	RANDOM {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			return destinations.random()
		}
	};

	abstract fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey

	companion object {
		val serializationType = EnumDataType(SortingOrder::class.java)
	}
}
