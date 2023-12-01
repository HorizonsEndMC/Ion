package net.horizonsend.ion.common.utils.redis.actions

import net.horizonsend.ion.common.utils.redis.RedisActions
import java.lang.reflect.Type

/** Broadcasts info, does not receive. No need to register on the broadcast end. */
abstract class RedisPubAction<Data>(val id: String, val type: Type, val runSync: Boolean) {
	/** Publish without calling the receiver */
	fun publish(data: Data) = RedisActions.publish(id, data, type)
}
