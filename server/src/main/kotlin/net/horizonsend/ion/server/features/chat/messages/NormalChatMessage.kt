package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class NormalChatMessage(
	senderLevel: Int,
	channel: ChatChannel,
	override val luckPermsPrefix: Component?,
	override val playerDisplayName: Component,
	override val luckPermsSuffix: Component?,
	override val message: Component,
	override val playerInfo: Component,

	override val color: TextColor,

	val sender: SLPlayerId
) : ChatMessage(senderLevel, channel.name)
