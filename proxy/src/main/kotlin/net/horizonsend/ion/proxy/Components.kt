package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.proxy.chat.ChannelManager
import net.horizonsend.ion.proxy.features.cache.Caches

val components = listOf(
	DBManager,
	RedisActions,
	Caches,
	ChannelManager
)
