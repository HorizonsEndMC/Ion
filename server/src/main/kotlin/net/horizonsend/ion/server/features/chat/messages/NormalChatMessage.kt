package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

data class NormalChatMessage(
	override val ionPrefix: Component?,
	override val luckPermsPrefix: Component?,
	override val playerDisplayName: Component,
	override val luckPermsSuffix: Component?,
	override val message: Component,
	override val playerInfo: Component,

	override val color: TextColor,

	val sender: SLPlayerId
) : ChatMessage()
