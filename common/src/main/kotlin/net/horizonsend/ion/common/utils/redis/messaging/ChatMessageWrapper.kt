package net.horizonsend.ion.common.utils.redis.messaging

import net.horizonsend.ion.common.utils.Server

data class ChatMessageWrapper(
	val actionId: String,
	val messageId: String,
	val serverId: String,
	val message: String,
	val targetServers: List<Server>
)

