package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.utils.text.addSpace
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor

abstract class ChatMessage {
	abstract val prefix: Component
	abstract val playerPrefix: Component
	abstract val playerDisplayName: Component
	abstract val playerSuffix: Component
	abstract val message: Component
	abstract val playerInfo: Component
	abstract val color: TextColor

	fun buildChatComponent(): Component = ofChildren(
		prefix.addSpace(false),
		playerPrefix.addSpace(false),
		playerDisplayName,
		playerSuffix.addSpace(true),
		text("» ", HEColorScheme.HE_DARK_GRAY),
		message.color(color),
	).hoverEvent(playerInfo)
}
