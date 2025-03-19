package net.horizonsend.ion.server.features.transport.items

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData.ItemExtractorMetaData
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
import net.kyori.adventure.text.Component

enum class SortingOrder(val displayName: Component) {
	NEAREST_FIRST(Component.text("Nearest First")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindingNodeWrapper>): PathfindingNodeWrapper {
			return destinations.minBy { wrapper -> wrapper.depth }
		}
	},
	ROUND_ROBIN(Component.text("Round Robin")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindingNodeWrapper>): PathfindingNodeWrapper {
			val currentIndex = extractorData.roundRobinIndex.toInt()
			val distanceIndexed = destinations.sortedBy { position -> position.depth }

			val nextIndex = (currentIndex + 1) % destinations.size
			extractorData.roundRobinIndex = nextIndex.toUShort()
			return distanceIndexed[nextIndex]
		}
	},
	FARTHEST_FIRST(Component.text("Farthest First")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindingNodeWrapper>): PathfindingNodeWrapper {
			return destinations.maxBy { wrapper -> wrapper.depth }
		}
	},
	RANDOM(Component.text("Random")) {
		override fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindingNodeWrapper>): PathfindingNodeWrapper {
			return destinations.random()
		}
	};

	abstract fun getDestination(extractorData: ItemExtractorMetaData, destinations: Collection<PathfindingNodeWrapper>): PathfindingNodeWrapper

	companion object {
		val serializationType = EnumDataType(SortingOrder::class.java)
	}
}
