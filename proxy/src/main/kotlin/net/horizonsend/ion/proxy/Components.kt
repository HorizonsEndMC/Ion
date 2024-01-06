package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.proxy.features.ConnectionMessages
import net.horizonsend.ion.proxy.features.ServerPresence
import net.horizonsend.ion.proxy.features.cache.Caches
import net.horizonsend.ion.proxy.features.messaging.PlayerTracking
import net.horizonsend.ion.proxy.managers.ReminderManager
import net.horizonsend.ion.proxy.utils.Discord

val components = listOf(
	DBManager,
	RedisActions.apply { ignoreUnknownMessages = true },
	Caches,
	ServerPresence,
	ConnectionMessages,
	PlayerTracking,
	ReminderManager,
	Discord
)
