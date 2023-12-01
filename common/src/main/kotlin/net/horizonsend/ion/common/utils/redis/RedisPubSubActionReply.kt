package net.horizonsend.ion.common.utils.redis

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.lang.reflect.Type

abstract class RedisPubSubActionReply<Data, ReplyType>(
	val replyAction: RedisPubSubAction<Pair<Data, ReplyType>>,
	id: String,
	type: Type,
	runSync: Boolean
): RedisPubSubAction<Data>(id, type, runSync) {
	final override fun onReceive(data: Data) {
		replyAction(data to createReply(data))
	}

	abstract fun createReply(data: Data): ReplyType

	fun call(data: Data): Deferred<ReplyType> {
		onReceive(data)

		RedisActions.publish(id, data, type)

		return CompletableDeferred()
	}
}
