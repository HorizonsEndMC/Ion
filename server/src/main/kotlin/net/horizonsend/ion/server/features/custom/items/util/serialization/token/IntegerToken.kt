package net.horizonsend.ion.server.features.custom.items.util.serialization.token

data object IntegerToken : SerializationToken<Int>() {
	override fun deserialize(serialized: String): Int {
		return serialized.toInt()
	}

	override fun storeValue(value: Int): String {
		return value.toString()
	}

	override fun validateValue(value: String): ValidationResult {
		if (runCatching { value.toInt() }.isFailure) return ValidationResult.Failure("$value is not a valid integer!")
		return ValidationResult.Success
	}

	override fun getValueRange(string: String): IntRange {
		if (string.isEmpty()) return 0..0
		return 0..string.takeWhile { it.isDigit() }.lastIndex
	}
}
