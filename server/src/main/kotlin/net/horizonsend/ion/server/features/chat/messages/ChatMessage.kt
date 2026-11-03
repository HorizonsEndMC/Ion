package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.features.chat.ChatChannel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

abstract class ChatMessage(
	val senderLevel: Int,
	val channelId: String,
	val senderPrestige: Int = 0
) {
	abstract val luckPermsPrefix: Component?
	abstract val playerDisplayName: Component
	abstract val luckPermsSuffix: Component?
	abstract val message: Component
	abstract val playerInfo: Component
	abstract val color: TextColor

	val channel get() = ChatChannel.valueOf(channelId)

	// TODO: Add more variations in color and potentially an icon or other visual indicator for prestige levels
	protected fun progressionPrefix(): Component {
		val levelColor = when (senderPrestige.coerceIn(0, 10) / 2) {
			0 -> NamedTextColor.AQUA
			1 -> NamedTextColor.GREEN
			2 -> NamedTextColor.YELLOW
			3 -> NamedTextColor.GOLD
			4 -> NamedTextColor.RED
			else -> NamedTextColor.LIGHT_PURPLE
		}
		val prestige = if (senderPrestige > 0) bracketed(text("\u2605$senderPrestige", NamedTextColor.GOLD)).append(space()) else Component.empty()
		return prestige.append(bracketed(text(senderLevel, levelColor)))
	}

	fun buildChatComponent(
		useLevelsPrefix: Boolean,
		useChannelPrefix: Boolean,
		useShortenedPrefix: Boolean,
		showLuckPermsPrefix: Boolean,
		additionalPrefix: Component? = null
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

		builder.append(additionalPrefix.orEmpty())

		if (showLuckPermsPrefix) luckPermsPrefix?.let { builder.append(it, space()) }
		builder.append(playerDisplayName)
		builder.append(luckPermsSuffix.orEmpty())
		builder.append(text(" » ", HEColorScheme.HE_DARK_GRAY))
		builder.append(message.color(color))

		return builder.build()
	}
}
