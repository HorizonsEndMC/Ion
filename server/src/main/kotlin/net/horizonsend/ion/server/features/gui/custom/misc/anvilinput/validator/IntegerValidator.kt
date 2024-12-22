package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED

object IntegerValidator : InputValidator {
	override fun isValid(input: String): ValidatorResult {
		if (input.toIntOrNull() == null) return ValidatorResult.FailureResult(Component.text("Not a valid number!", RED))
		return ValidatorResult.SuccessResult
	}
}
