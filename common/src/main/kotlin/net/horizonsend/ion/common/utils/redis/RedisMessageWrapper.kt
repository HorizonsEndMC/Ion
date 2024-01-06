package net.horizonsend.ion.common.utils.redis

import net.horizonsend.ion.common.utils.Server

data class RedisMessageWrapper(
	val actionId: String,
	val messageId: String,
	val serverId: String,
	val message: String,
	val targetServers: List<Server>
)

