package net.horizonsend.ion.server.features.custom.items.util.serialization.token

class StringToken : SerializationToken<String>() {
	override fun deserialize(serialized: String): String {
		return serialized.removePrefix("\"").removeSuffix("\"")
	}

	override fun validateValue(value: String): ValidationResult {
		if (value.isEmpty()) return ValidationResult.Failure("Empty token: Expected \".")
		if (!value.startsWith("\"")) return ValidationResult.Failure("Expected \"', found ${value.first()}")
		if (!value.endsWith("\"")) return ValidationResult.Failure("Expected '\"', found ${value.last()}")

		return ValidationResult.Success
	}

	override fun storeValue(value: String): String {
		return "\"$value\""
	}
}
