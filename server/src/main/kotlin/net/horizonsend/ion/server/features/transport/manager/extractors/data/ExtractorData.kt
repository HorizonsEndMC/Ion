package net.horizonsend.ion.server.features.transport.manager.extractors.data

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class ExtractorData(val pos: BlockKey) {
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

	class StandardExtractorData(key: BlockKey) : ExtractorData(key)
}
