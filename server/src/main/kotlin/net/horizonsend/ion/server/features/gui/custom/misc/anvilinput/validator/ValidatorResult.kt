package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

import net.kyori.adventure.text.Component

sealed interface ValidatorResult {
	val success: Boolean

	sealed interface Success : ValidatorResult {
		override val success: Boolean get() = true
	}

	data object SuccessResult : Success {
		override val success: Boolean = true
	}

	data class ResultsResult(val results: Collection<Component>) : Success {
		override val success: Boolean = true
	}

	data class FailureResult(val message: Component) : ValidatorResult {
		override val success: Boolean = false
	}
}
