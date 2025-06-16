package net.horizonsend.ion.common.utils.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfiguration(
	val token: String = "",
	val guild: Long = 0,
	val globalChannel: Long = 0,
	val eventsChannel: Long = 0,
	val changelogChannel: Long = 0,
	val approvalQueueChannel: Long = 0,
	val serverNewsChannel: Long = 0,
	val moderatorRole: Long = 0,
)

