package net.horizonsend.ion.common.utils.input

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.util.function.Consumer

interface InputResult : PotentiallyFutureResult {
	fun isSuccess(): Boolean

	fun getReason(): List<Component>?

	override fun sendReason(audience: Audience) { getReason()?.forEach(audience::sendMessage) }

	fun sendReasonIfFailure(audience: Audience) {
		if (!isSuccess()) sendReason(audience)
	}

	fun sendReasonIfSuccess(audience: Audience) {
		if (isSuccess()) sendReason(audience)
	}

	override fun withResult(consumer: Consumer<InputResult>) = consumer.accept(this)

	override fun get(): InputResult = this

	interface Success : InputResult {
		override fun isSuccess(): Boolean = true
	}

	data object InputSuccess : Success {
		override fun getReason(): List<Component>? = null
	}

	data class SuccessReason(val reasonText: List<Component>) : Success {
		override fun getReason(): List<Component> = reasonText
	}

	interface Failure : InputResult {
		override fun isSuccess(): Boolean = false
	}

	data object InputFailure : Failure {
		override fun getReason(): List<Component>? = null
	}

	data class FailureReason(val reasonText: List<Component>) : Failure {
		override fun getReason(): List<Component> = reasonText
	}
}
