package net.horizonsend.ion.common.redis

import net.horizonsend.ion.common.ServerType

data class RedisMessageWrapper(
	val actionId: String,
	val messageId: String,
	val serverId: String,
	val message: String,
	val targetServers: List<ServerType>
)

