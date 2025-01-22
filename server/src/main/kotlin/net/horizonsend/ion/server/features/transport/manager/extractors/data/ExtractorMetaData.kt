package net.horizonsend.ion.server.features.transport.manager.extractors.data

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

interface ExtractorMetaData {
	val key: BlockKey

	fun toExtractorData(): ExtractorData
}
