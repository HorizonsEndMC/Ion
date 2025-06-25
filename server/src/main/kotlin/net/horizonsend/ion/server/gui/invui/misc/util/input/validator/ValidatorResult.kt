package net.horizonsend.ion.server.gui.invui.misc.util.input.validator

import net.horizonsend.ion.common.utils.input.InputResult
import net.kyori.adventure.text.Component

sealed interface ValidatorResult<T : Any> : InputResult {
	val result: T?

	sealed interface ValidatorSuccess<T : Any> : ValidatorResult<T> {
		override val result: T

		override fun isSuccess(): Boolean = true
	}

	data object ValidatorSuccessEmpty : ValidatorSuccess<Any> {
		override fun getReason(): List<Component>? = null

		override val result: Any = Any()
	}

	data class ValidatorSuccessMultiEntry<T : Any>(val results: Collection<T>) : ValidatorSuccess<T> {
		override fun getReason(): List<Component>? = null

		override val result: T = results.first()
	}

	data class ValidatorSuccessSingleEntry<T : Any>(override val result: T) : ValidatorSuccess<T> {
		override fun getReason(): List<Component>? = null
	}

	data class FailureResult<T : Any>(val message: List<Component>) : ValidatorResult<T> {
		constructor(message: Component) : this(listOf(message))

		override fun getReason(): List<Component> {
			return message
		}

		override val result: T? = null
		override fun isSuccess(): Boolean = false
	}
}
