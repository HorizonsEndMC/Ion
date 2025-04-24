package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

class CombinedSolarPanel(private val originCache: SolarPanelCache, origin: BlockKey) {
	var origin: BlockKey = origin; private set
	val extractorPositions: ConcurrentHashMap.KeySetView<BlockKey, Boolean> = ConcurrentHashMap.newKeySet()

	init {
	    addPosition(origin)
	}

	private var lastTicked = System.currentTimeMillis()

	private fun getDelta(time: Long): Double {
		val diff = time - lastTicked
		return (diff.toDouble() / ConfigurationFiles.transportSettings().extractorConfiguration.extractorTickIntervalMS.toDouble())
	}

	fun markTicked(): Double {
		val now = System.currentTimeMillis()
		val delta = getDelta(now)
		lastTicked = now
		return delta
	}

	fun tick(delta: Double) {
		if (extractorPositions.isEmpty()) return

		return originCache.powerCache.tickCombinedSolarPanel(this, delta)
	}

	/**
	 * Verifies the structure, and attempts to grow if new panels are added.
	 *
	 * Returns false if there are no positions remaining
	 **/
	fun verifyIntegrity() {
		if (extractorPositions.isEmpty()) {
			originCache.combinedSolarPanels.remove(this)
			return
		}

		@Suppress("UNCHECKED_CAST")
		extractorPositions.toArray(arrayOfNulls<BlockKey>(0) as Array<BlockKey>).forEach(::verifyPosition)

		return
	}

	fun verifyPosition(blockKey: BlockKey) {
		if (!extractorPositions.contains(blockKey)) return
		if (originCache.isSolarPanel(blockKey)) return

		originCache.combinedSolarPanelPositions.remove(blockKey)
		extractorPositions.remove(blockKey)

		if (extractorPositions.isEmpty()) {
			originCache.combinedSolarPanels.remove(this)
			return
		}

		if (blockKey == origin) {
			origin = extractorPositions.random()
		}
	}

	fun addPosition(position: BlockKey) {
		originCache.combinedSolarPanelPositions[position] = this
		originCache.combinedSolarPanels.add(this)
		extractorPositions.add(position)
	}

	fun addPositions(positions: Collection<BlockKey>) {
		extractorPositions.addAll(positions)

		originCache.combinedSolarPanels.add(this)

		for (position in positions) {
			originCache.combinedSolarPanelPositions[position] = this
		}
	}

	fun removePosition(blockKey: BlockKey) {
		originCache.combinedSolarPanelPositions.remove(blockKey)
		extractorPositions.remove(blockKey)

		verifyIntegrity()
	}

	fun removePositions(positions: Set<BlockKey>) {
		extractorPositions.removeAll(positions.toSet())
		positions.forEach(originCache.combinedSolarPanelPositions::remove)

		verifyIntegrity()
	}

	fun getPositions(): Set<BlockKey> = extractorPositions

	fun getPower(delta: Double): Int {
		return extractorPositions.sumOf { originCache.getPower(it, delta) }
	}
}
