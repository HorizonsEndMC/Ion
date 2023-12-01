package net.horizonsend.ion.common.utils.redis

import java.lang.reflect.Type

abstract class RedisPubSubAction<Data>(id: String, type: Type, runSync: Boolean) : RedisPubAction<Data>(id, type, runSync) {
	abstract fun onReceive(data: Data)

	// can't actually cast it outside of this class in the redis pub sub listener so we have to do it here.
	// ugly, but it works
	@Suppress("UNCHECKED_CAST")
	internal fun castAndReceive(data: Any): Unit = onReceive(data as Data)

	/** Publish and call the receiver */
	open override operator fun invoke(data: Data) {
		onReceive(data)
		publish(data)
	}
}
