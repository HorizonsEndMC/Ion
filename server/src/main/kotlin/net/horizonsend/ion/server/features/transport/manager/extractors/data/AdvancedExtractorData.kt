package net.horizonsend.ion.server.features.transport.manager.extractors.data

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class AdvancedExtractorData<C: Any>(
	pos: BlockKey,
	val metaData: C
) : ExtractorData(pos) {
	abstract val serializer: PDCSerializers.RegisteredSerializer<C>

	fun asMetaDataContainer(): MetaDataContainer<C, PDCSerializers.RegisteredSerializer<C>> = MetaDataContainer(serializer, metaData)
}
