package net.horizonsend.ion.common

import net.horizonsend.ion.common.utils.redis.RedisActions
import net.horizonsend.ion.common.utils.redis.RedisPubSubAction

abstract class IonComponent {
	protected val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

	open fun onEnable() {}

	open fun onDisable() {}

	inline fun <reified T, B> ((T) -> B).registerRedisAction(id: String, runSync: Boolean = true): RedisPubSubAction<T> {
		return RedisActions.register(id, runSync, this)
	}
}
