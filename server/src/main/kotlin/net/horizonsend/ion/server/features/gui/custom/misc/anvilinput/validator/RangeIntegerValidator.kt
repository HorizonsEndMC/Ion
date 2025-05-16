package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED

class RangeIntegerValidator(val range: IntRange) : InputValidator<Int> {
	override fun isValid(input: String): ValidatorResult<Int> {
		val intResult = input.toIntOrNull() ?: return ValidatorResult.FailureResult(Component.text("Not a valid number!", RED))
		if (intResult !in range) return ValidatorResult.FailureResult(Component.text("Number must be between ${range.first} and ${range.last}", RED))

		return ValidatorResult.ValidatorSuccessSingleEntry(input, intResult)
	}
}
