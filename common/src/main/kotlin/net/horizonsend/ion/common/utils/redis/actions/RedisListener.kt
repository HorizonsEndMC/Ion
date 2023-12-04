package net.horizonsend.ion.common.utils.redis.actions

import java.lang.reflect.Type

interface RedisListener<Data> {
	val type: Type
	val id: String

	fun onReceive(data: Data)

	// can't actually cast it outside of this class in the redis pub sub listener so we have to do it here.
	// ugly, but it works
	@Suppress("UNCHECKED_CAST")
	fun castAndReceive(data: Any): Unit = onReceive(data as Data)
}
