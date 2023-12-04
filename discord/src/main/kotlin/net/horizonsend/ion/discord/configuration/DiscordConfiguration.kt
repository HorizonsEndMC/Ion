package net.horizonsend.ion.discord.configuration

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfiguration(
	val discordBotToken: String = "",
	val guildID: Long = 0,
	val globalChannelId: Long = 0
)

