package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.proxy.features.ConnectionMessages
import net.horizonsend.ion.proxy.features.PlayerTracking
import net.horizonsend.ion.proxy.features.ServerMapping
import net.horizonsend.ion.proxy.features.ServerPresence
import net.horizonsend.ion.proxy.features.cache.Caches
import net.horizonsend.ion.proxy.managers.ReminderManager

val components = listOf(
	DBManager,
	RedisActions,
	Caches,
	ServerMapping,
	ServerPresence,
	ConnectionMessages,
	ReminderManager,
	PlayerTracking
)
