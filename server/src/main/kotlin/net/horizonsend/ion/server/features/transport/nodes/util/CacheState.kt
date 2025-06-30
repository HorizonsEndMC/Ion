package net.horizonsend.ion.server.features.transport.nodes.util

abstract class CacheState<T : Any> {
	class Empty<T : Any> : CacheState<T>() {
		override fun get(): T? {
			return null
		}
	}

	data class Present<T : Any>(val node: T): CacheState<T>() {
		override fun get(): T {
			return node
		}
	}

	abstract fun get(): T?
}
