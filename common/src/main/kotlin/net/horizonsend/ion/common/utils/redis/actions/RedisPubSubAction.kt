package net.horizonsend.ion.common.utils.redis.actions

import java.lang.reflect.Type

/**
 * Broadcasts and receives messages.
 **/
abstract class RedisPubSubAction<Data>(id: String, type: Type, runSync: Boolean) : RedisPubAction<Data>(id, type, runSync), RedisListener<Data> {
	/** Publish and call the receiver */
	operator fun invoke(data: Data) {
		onReceive(data)
		publish(data)
	}
}
