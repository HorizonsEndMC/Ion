package net.horizonsend.ion.discord

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.discord.caches.Caches
import net.horizonsend.ion.discord.features.PlayerTracking
import net.horizonsend.ion.discord.features.PresenceManager

val components = listOf(
	DBManager,
	RedisActions,
	Caches,
	PresenceManager,
	PlayerTracking
)
