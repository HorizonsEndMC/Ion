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

	override fun getValueRange(string: String): IntRange {
		if (string[0] != '"')  throw IllegalArgumentException("String is not properly opened!")

		val minusOpening = string.substring(1..string.lastIndex)

		var lastChar: Char? = null
		val index =  minusOpening.indexOfFirst { char ->
			val escaped = lastChar != null && lastChar == '\\'
			lastChar = char
			char == '"'  && !escaped
		}

		if (index == -1) throw IllegalArgumentException("String is not properly closed!")
		return 1.. index
	}
}
