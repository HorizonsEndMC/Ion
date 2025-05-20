package net.horizonsend.ion.server.gui.invui.input.validator

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.RED

object IntegerValidator : InputValidator<Int> {
	override fun isValid(input: String): ValidatorResult<Int> {
		val int = input.toIntOrNull() ?: return ValidatorResult.FailureResult(Component.text("Not a valid number!", RED))
		return ValidatorResult.ValidatorSuccessSingleEntry(input, int)
	}
}
