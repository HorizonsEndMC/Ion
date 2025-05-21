package net.horizonsend.ion.server.gui.invui.input.validator

import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED

class RangeDoubleValidator(val range: ClosedFloatingPointRange<Double>) : InputValidator<Double> {
	override fun isValid(input: String): ValidatorResult<Double> {
		val doubleResult = input.toDoubleOrNull() ?: return ValidatorResult.FailureResult(Component.text("Not a valid number!", RED))
		if (doubleResult !in range) return ValidatorResult.FailureResult(Component.text("Number must be between ${range.start.roundToHundredth()} and ${range.endInclusive.roundToHundredth()}", RED))

		return ValidatorResult.ValidatorSuccessSingleEntry(doubleResult)
	}
}
