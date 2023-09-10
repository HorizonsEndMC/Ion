package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions
import net.horizonsend.ion.proxy.features.cache.Caches
import net.horizonsend.ion.proxy.features.cache.ConnectionMessages
import net.horizonsend.ion.proxy.features.cache.ServerMapping
import net.horizonsend.ion.proxy.features.cache.ServerPresence

val components = listOf(
	DBManager,
	RedisActions,
	Caches,
	ServerMapping,
	ServerPresence,
	ConnectionMessages,
)
