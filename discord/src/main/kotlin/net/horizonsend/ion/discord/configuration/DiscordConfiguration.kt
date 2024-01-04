package net.horizonsend.ion.discord.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.discord.Channel

@Serializable
data class DiscordConfiguration(
	val discordBotToken: String = "",
	val guildID: Long = 0,
	val globalChannelId: Long = 0,
	val channelIdMap: Map<Channel, Long> = mapOf(
		Channel.GLOBAL to 954781227246817341,
		Channel.EVENTS to 979124611256041473,
		Channel.CHANGELOG to 1096907580577697833
	)
)

