package net.horizonsend.ion.server.features.chat.messages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

data class NormalChatMessage(
	override val prefix: Component,
	override val playerPrefix: Component,
	override val playerDisplayName: Component,
	override val playerSuffix: Component,
	override val message: Component,
	override val playerInfo: Component,

	override val color: TextColor
) : ChatMessage()
