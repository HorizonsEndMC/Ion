package net.horizonsend.ion.discord

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.redisaction.RedisActions

val components = listOf<IonComponent>(
	DBManager,
	RedisActions
)
