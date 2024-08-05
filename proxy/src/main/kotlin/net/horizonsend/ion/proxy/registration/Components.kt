package net.horizonsend.ion.proxy.registration

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.redis.RedisActions
import net.horizonsend.ion.proxy.features.ConnectionMessages
import net.horizonsend.ion.proxy.features.ReminderManager
import net.horizonsend.ion.proxy.features.ServerPresence
import net.horizonsend.ion.proxy.features.cache.Caches
import net.horizonsend.ion.proxy.features.discord.Discord
import net.horizonsend.ion.proxy.features.messaging.PlayerTracking
import net.horizonsend.ion.proxy.features.misc.ServerMessaging

val components = listOf(
	DBManager,
	RedisActions.apply { ignoreUnknownMessages = true },
	Caches,
	ServerPresence,
	ConnectionMessages,
	PlayerTracking,
	ReminderManager,
	Discord,
	ServerMessaging
)
