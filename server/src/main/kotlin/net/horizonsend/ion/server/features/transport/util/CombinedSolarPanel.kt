package net.horizonsend.ion.server.features.transport.util

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import java.util.concurrent.ConcurrentHashMap

class CombinedSolarPanel(val originCache: SolarPanelCache, origin: BlockKey) {
	private var extractorPositions = ConcurrentHashMap.newKeySet<BlockKey>().apply { add(origin) }

	fun tick() {

	}

	/**
	 * Verifies the structure, and attempts to grow if new panels are added.
	 *
	 * Returns false if there are no positions remaining
	 **/
	fun verifyIntegrity(): Boolean {
		gatherExtractorPositions()
		return !extractorPositions.isEmpty()
	}

	private fun gatherExtractorPositions() {
		// Start with current positions
		val visitQueue = ArrayDeque<BlockKey>(extractorPositions)
		val visited = LongOpenHashSet()

		val foundPositions = ConcurrentHashMap.newKeySet<BlockKey>()

		var iterations = 0L
		val upperBound = 20_000

		while (visitQueue.isNotEmpty() && iterations < upperBound) {
			iterations++
			val current = visitQueue.removeFirst()
			visited.add(current)

			if (originCache.isSolarPanel(current)) {
				foundPositions.add(current)
			}

			val toVisit = CARDINAL_BLOCK_FACES.mapNotNull {
				val relativeKey = getRelative(current, it)
				if (visitQueue.contains(relativeKey)) return@mapNotNull null

				relativeKey
			}

			visitQueue.addAll(toVisit)
		}

		extractorPositions = foundPositions
	}

	private fun removePositions() {

	}

	fun getPositions(): Set<BlockKey> = extractorPositions
	
	companion object {
		fun new(cache: SolarPanelCache, extractorPosition: BlockKey) {
			val new = CombinedSolarPanel(cache, extractorPosition)
			new.verifyIntegrity()
		}
	}
}
