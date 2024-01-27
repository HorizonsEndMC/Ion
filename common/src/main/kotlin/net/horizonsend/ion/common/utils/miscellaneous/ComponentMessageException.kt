package net.horizonsend.ion.common.utils.miscellaneous

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED

abstract class ComponentMessageException(override val message: String) : RuntimeException(message) {
	open fun formatMessage(): Component {
		return text(message, RED)
	}

	open fun sendMessage(audience: Audience) = audience.sendMessage(formatMessage())
}
