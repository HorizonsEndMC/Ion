package net.horizonsend.ion.server.features.transport.items

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.kyori.adventure.text.Component

enum class SortingOrder(val displayName: Component) {
	NEAREST_FIRST(Component.text("Nearest First")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindResult>): PathfindResult {
			return destinations.minBy { wrapper -> wrapper.trackedPath.length }
		}
	},
	ROUND_ROBIN(Component.text("Round Robin")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindResult>): PathfindResult {
			val currentIndex = extractorData.roundRobinIndex.toInt()
			val distanceIndexed = destinations.sortedBy { position -> position.trackedPath.length }

			val nextIndex = (currentIndex + 1) % destinations.size
			extractorData.roundRobinIndex = nextIndex.toUShort()
			return distanceIndexed[nextIndex]
		}
	},
	FARTHEST_FIRST(Component.text("Farthest First")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindResult>): PathfindResult {
			return destinations.maxBy { wrapper -> wrapper.trackedPath.length }
		}
	},
	RANDOM(Component.text("Random")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindResult>): PathfindResult {
			return destinations.random()
		}
	};

	abstract fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindResult>): PathfindResult

	companion object {
		val serializationType = EnumDataType(SortingOrder::class.java)
	}
}
