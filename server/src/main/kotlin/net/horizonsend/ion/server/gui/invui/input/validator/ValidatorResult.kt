package net.horizonsend.ion.server.gui.invui.input.validator

import net.horizonsend.ion.common.utils.InputResult
import net.kyori.adventure.text.Component

sealed interface ValidatorResult<T : Any> : InputResult {
	val result: T?

	sealed interface ValidatorSuccess<T : Any> : ValidatorResult<T> {
		override val result: T

		override fun isSuccess(): Boolean = true
	}

	data class ValidatorSuccessEmpty(val rawResult: String) : ValidatorSuccess<Any> {
		override fun getReason(): List<Component>? = null

		override val result: Any = Any()
	}

	data class ValidatorSuccessMultiEntry<T : Any>(val input: String, val results: Collection<T>) : ValidatorSuccess<T> {
		override fun getReason(): List<Component>? = null

		override val result: T = results.first()
	}

	data class ValidatorSuccessSingleEntry<T : Any>(val rawResult: String, override val result: T ) : ValidatorSuccess<T> {
		override fun getReason(): List<Component>? = null
	}

	data class FailureResult<T : Any>(val message: Component) : ValidatorResult<T> {
		override fun getReason(): List<Component> {
			return listOf(message)
		}

		override val result: T? = null
		override fun isSuccess(): Boolean = false
	}
}
