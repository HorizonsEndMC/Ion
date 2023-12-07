package net.horizonsend.ion.common.utils.redis

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

abstract class RedisAction<Data>(val id: String, val type: Type, val runSync: Boolean) {
	abstract fun onReceive(data: Data)

	// can't actually cast it outside of this class in the redis pub sub listener so we have to do it here.
	// ugly, but it works
	@Suppress("UNCHECKED_CAST")
	internal fun castAndReceive(data: Any): Unit = onReceive(data as Data)

	operator fun invoke(data: Data) {
		onReceive(data)
		RedisActions.publishMessage(id, data, type)
	}

	companion object {
		fun <Data> noOpAction(id: String): RedisAction<Data> = object : RedisAction<Data>(id, object : TypeToken<Data>() {}.type, false) {
			// Do nothing
			override fun onReceive(data: Data) {}
		}
	}
}
