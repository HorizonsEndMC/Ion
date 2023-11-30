package net.horizonsend.ion.discord.configuration

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfiguration(
	val discordBotToken: String = "",
	val guildID: Long = 771506479412019224,
	val globalChannelId: Long = 1060343929473601536
)

