package net.horizonsend.ion.common

import net.horizonsend.ion.common.utils.redis.RedisAction
import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.RedisActions.register

abstract class IonComponent {
	protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

	open fun onEnable() {}

	open fun onDisable() {}

	inline fun <reified T, B> ((T) -> B).registerRedisAction(id: String, runSync: Boolean = true): RedisAction<T> {
		return RedisActions.createAction(id, runSync, this).register()
	}

	inline fun <reified T, B> ((T) -> B).createRedisAction(id: String, runSync: Boolean = true): RedisAction<T> {
		return RedisActions.createAction(id, runSync, this)
	}
}
