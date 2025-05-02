package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

import net.kyori.adventure.text.Component

sealed interface ValidatorResult<T : Any> {
	val result: T?

	fun isSuccess(): Boolean

	sealed interface ValidatorSuccess<T : Any> : ValidatorResult<T> {
		override val result: T

		override fun isSuccess(): Boolean = true
	}

	data class ValidatorSuccessEmpty(val rawResult: String) : ValidatorSuccess<Any> {
		override val result: Any = Any()
	}

	data class ValidatorSuccessMultiEntry<T : Any>(val input: String, val results: Collection<T>) : ValidatorSuccess<T> {
		override val result: T = results.first()
	}

	data class ValidatorSuccessSingleEntry<T : Any>(val rawResult: String, override val result: T ) : ValidatorSuccess<T>

	data class FailureResult<T : Any>(val message: Component) : ValidatorResult<T> {
		override val result: T? = null
		override fun isSuccess(): Boolean = false
	}
}
