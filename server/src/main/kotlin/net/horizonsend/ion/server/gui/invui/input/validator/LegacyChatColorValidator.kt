package net.horizonsend.ion.server.gui.invui.input.validator

import net.kyori.adventure.text.Component.text
import net.md_5.bungee.api.ChatColor

object LegacyChatColorValidator : InputValidator<ChatColor> {
	override fun isValid(input: String): ValidatorResult<ChatColor> {
		val colorResult = runCatching { ChatColor.valueOf(input.uppercase()) }.getOrNull()
		if (colorResult == null) {
			return ValidatorResult.FailureResult(text("Must be one of ${ChatColor.values().joinToString { it.name }}"))
		}

		return ValidatorResult.ValidatorSuccessSingleEntry(colorResult)
	}
}
