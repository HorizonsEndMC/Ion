package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED

class RangeIntegerValidator(val range: IntRange) : InputValidator {
	override fun isValid(input: String): ValidatorResult {
		val intResult = input.toIntOrNull()
		if (intResult == null) return ValidatorResult.FailureResult(Component.text("Not a valid number!", RED))
		if (intResult !in range) return ValidatorResult.FailureResult(Component.text("Number must be between ${range.min()} and ${range.max()}", RED))

		return ValidatorResult.SuccessResult
	}
}
