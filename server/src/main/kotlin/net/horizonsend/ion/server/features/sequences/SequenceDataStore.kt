package net.horizonsend.ion.server.features.sequences

import java.util.Optional

class SequenceDataStore() {
	val keyedData = mutableMapOf<String, Any?>()

	operator fun <T : Any> get(key: String): Optional<T> {
		if (!keyedData.containsKey(key)) return Optional.empty<T>()
		@Suppress("UNCHECKED_CAST")
		return Optional.ofNullable(keyedData[key] as? T)
	}

	operator fun <T : Any> set(key: String, value: T) {
		keyedData[key] = value
	}
}
