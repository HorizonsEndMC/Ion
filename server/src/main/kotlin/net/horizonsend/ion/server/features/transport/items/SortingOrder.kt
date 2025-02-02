package net.horizonsend.ion.server.features.transport.items

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component

enum class SortingOrder(val displayName: Component) {
	NEAREST_FIRST(Component.text("Nearest First")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			val extractorPosition = toVec3i(extractorData.key)
			return destinations.minBy { key -> extractorPosition.distance(toVec3i(key)) }
		}
	},
	ROUND_ROBIN(Component.text("Round Robin")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			val currentIndex = extractorData.roundRobinIndex.toInt()

			val extractorPosition = toVec3i(extractorData.key)
			val distanceIndexed = destinations.sortedBy { key -> distanceSquared(toVec3i(key), extractorPosition) }

			val nextIndex = (currentIndex + 1) % destinations.size
			extractorData.roundRobinIndex = nextIndex.toUShort()
			return distanceIndexed[nextIndex]
		}
	},
	FARTHEST_FIRST(Component.text("Farthest First")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			val extractorPosition = toVec3i(extractorData.key)
			return destinations.maxBy { key -> extractorPosition.distance(toVec3i(key)) }
		}
	},
	RANDOM(Component.text("Random")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey {
			return destinations.random()
		}
	};

	abstract fun getDestination(extractorData: ItemExtractorMetaData, destinations: List<BlockKey>): BlockKey

	companion object {
		val serializationType = EnumDataType(SortingOrder::class.java)
	}
}
