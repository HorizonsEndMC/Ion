package net.horizonsend.ion.server.features.custom.items.util.serialization.token

abstract class SerializationToken<T> {
	abstract fun deserialize(serialized: String): T
	abstract fun storeValue(value: T): String
	abstract fun validateValue(value: String): ValidationResult
}
