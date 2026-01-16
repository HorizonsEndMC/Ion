package net.horizonsend.ion.server.features.sequences

import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializers
import java.util.Optional

class SequenceDataStore(val keyedData: MutableMap<String, Any> = mutableMapOf(), val context: SequenceContext) {
	val metaDataMirror = mutableMapOf<String, MetaDataContainer<*, *>>()

	operator fun <T : Any> get(key: String): Optional<T> {
		if (!keyedData.containsKey(key)) return Optional.empty<T>()
		@Suppress("UNCHECKED_CAST")
		return Optional.ofNullable(keyedData[key] as? T)
	}

	fun <T : Any> set(key: String, value: T) {
		metaDataMirror[key] = PDCSerializers.pack(value)
		keyedData[key] = value
	}
}
