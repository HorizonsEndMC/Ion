package net.horizonsend.ion.discord.configuration

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfiguration(
	val discordBotToken: String = "MTA2MDM0MTc0MDQyODU5MTE0NQ.GSd4kg.igUpsi0ba5NVqLJy5v34gGkTnZylyGlDY9nFoY",
	val guildID: Long = 771506479412019221,
	val globalChannelId: Long = 1060343929473601536
)

