package net.horizonsend.ion.common.utils.redis

import java.lang.reflect.Type

abstract class RedisPubAction<Data>(val id: String, val type: Type, val runSync: Boolean) {
	open operator fun invoke(data: Data) {
		publish(data)
	}

	/** Publish without calling the receiver */
	fun publish(data: Data) = RedisActions.publish(id, data, type)
}
