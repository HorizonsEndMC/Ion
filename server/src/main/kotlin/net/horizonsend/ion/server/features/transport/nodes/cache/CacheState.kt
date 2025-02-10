package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.nodes.types.Node

sealed interface CacheState {
	data object Empty: CacheState {
		override fun get(): Node? {
			return null
		}
	}

	data class Present(val node: Node): CacheState {
		override fun get(): Node {
			return node
		}
	}

	fun get(): Node?
}
