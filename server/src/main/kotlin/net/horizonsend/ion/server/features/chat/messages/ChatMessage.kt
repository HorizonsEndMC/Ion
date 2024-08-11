package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor

abstract class ChatMessage {
	abstract val ionPrefix: Component?
	abstract val luckPermsPrefix: Component?
	abstract val playerDisplayName: Component
	abstract val luckPermsSuffix: Component?
	abstract val message: Component
	abstract val playerInfo: Component
	abstract val color: TextColor

	fun buildChatComponent(): Component = ofChildren(
		ionPrefix.orEmpty(),
		luckPermsPrefix.orEmpty(),
		playerDisplayName,
		luckPermsSuffix.orEmpty(),
		text(" » ", HEColorScheme.HE_DARK_GRAY),
		message.color(color),
	).hoverEvent(playerInfo)
}
