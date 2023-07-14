package net.horizonsend.ion.common

import net.horizonsend.ion.common.utils.redisaction.RedisAction
import net.horizonsend.ion.common.utils.redisaction.RedisActions

abstract class IonComponent {
	protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

	open fun onEnable() {}

	open fun onDisable() {}

	inline fun <reified T, B> ((T) -> B).registerRedisAction(id: String, runSync: Boolean = true): RedisAction<T> {
		return RedisActions.register(id, runSync, this)
	}

	open fun vanillaOnly(): Boolean = false
}
