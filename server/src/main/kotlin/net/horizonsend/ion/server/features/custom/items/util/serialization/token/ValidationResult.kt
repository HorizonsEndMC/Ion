package net.horizonsend.ion.server.features.custom.items.util.serialization.token

sealed interface ValidationResult {
	data object Success : ValidationResult
	data class Failure(val message: String) : ValidationResult
	data class Error(val error: Throwable) : ValidationResult
}
