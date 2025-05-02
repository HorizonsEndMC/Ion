package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.types.Node

abstract class CacheState {
	data object Empty: CacheState() {
		override fun get(): Node? {
			return null
		}
	}

	data class Present(val node: Node): CacheState() {
		override fun get(): Node {
			return node
		}
	}

	abstract fun get(): Node?
}
