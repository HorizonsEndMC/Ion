package net.horizonsend.ion.common.utils.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfiguration(
	val token: String = "",
	val guild: Long = 0,
	val globalChannel: Long = 0,
	val eventsChannel: Long = 0,
	val changelogChannel: Long = 0
)

