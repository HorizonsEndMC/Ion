package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor

class NationsChatMessage<A : DbObject>(
	senderLevel: Int,
	channel: ChatChannel,

	/** Settlement, or nation ID */
	val nationsid: Oid<A>,

	val nationName: Component,
	val nationsRole: Component,

	val settlementName: Component,
	val settlementRole: Component,

	override val luckPermsPrefix: Component?,
	override val playerDisplayName: Component,
	override val luckPermsSuffix: Component?,
	override val message: Component,
	override val playerInfo: Component,

	override val color: TextColor,
	senderPrestige: Int = 0
) : ChatMessage(senderLevel, channel.name, senderPrestige) {

	fun buildChatComponent(
		useLevelsPrefix: Boolean,
		useChannelPrefix: Boolean,
		useShortenedPrefix: Boolean,
		showLuckPermsPrefix: Boolean,
		showSettlementNamePrefix: Boolean,
		showSettlementRolePrefix: Boolean,
		showNationNamePrefix: Boolean,
		showNationRolePrefix: Boolean,
	): Component {
		val builder = text()
		builder.hoverEvent(playerInfo)

		if (useLevelsPrefix) builder.append(progressionPrefix(), space())
		if (useChannelPrefix) {
			if (useShortenedPrefix) {
				builder.append(channel.shortenedChannelPrefix, space())
			} else {
				builder.append(channel.channelPrefix, space())
			}
		}

		if (showSettlementNamePrefix) builder.append(settlementName, space())
		if (showSettlementRolePrefix) builder.append(settlementRole, space())
		if (showNationNamePrefix) builder.append(nationName, space())
		if (showNationRolePrefix) builder.append(nationsRole, space())

		if (showLuckPermsPrefix) luckPermsPrefix?.let { builder.append(it, space()) }
		builder.append(playerDisplayName)
		builder.append(luckPermsSuffix.orEmpty())
		builder.append(text(" » ", HEColorScheme.HE_DARK_GRAY))
		builder.append(message.color(color))

		return builder.build()
	}
}
