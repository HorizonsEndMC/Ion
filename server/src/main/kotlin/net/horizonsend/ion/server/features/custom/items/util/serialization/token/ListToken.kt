package net.horizonsend.ion.server.features.custom.items.util.serialization.token

open class ListToken<T>(private val childType: SerializationToken<T>) : SerializationToken<List<T>>() {
	private fun isListOpen(char: Char, previous: Char): Boolean {
		if (previous == '\\') return false
		return char == '['
	}

	private fun isListClose(char: Char, previous: Char): Boolean {
		if (previous == '\\') return false
		return char == ']'
	}

	override fun storeValue(value: List<T>): String {
		val builder = StringBuilder()

		builder.append("[")

		val tokens = value.map { childType.storeValue(it) }
		val iterator = tokens.iterator()

		while (iterator.hasNext()) {
			val token = iterator.next()
			builder.append(token)

			if (iterator.hasNext()) {
				builder.append(", ")
			}
		}

		builder.append("]")

		return builder.toString()
	}

	override fun deserialize(serialized: String): List<T> {
		val values = serialized.removePrefix("[").removeSuffix("]").split(',')
		return values.map { childType.deserialize(it) }
	}

	override fun validateValue(value: String): ValidationResult {
		if (value.isEmpty()) return ValidationResult.Failure("Empty token: Expected [.")
		if (!value.startsWith("[")) return ValidationResult.Failure("Expected '[', found ${value.first()}")
		if (!value.endsWith("[")) return ValidationResult.Failure("Expected ']', found ${value.last()}")

		return ValidationResult.Success
	}
}
