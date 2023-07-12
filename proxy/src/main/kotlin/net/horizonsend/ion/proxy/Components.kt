package net.horizonsend.ion.proxy

import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.proxy.chat.ChannelManager
import net.horizonsend.ion.proxy.features.cache.Caches

val components = listOf(
	DBManager,
	Caches,
	ChannelManager
)
