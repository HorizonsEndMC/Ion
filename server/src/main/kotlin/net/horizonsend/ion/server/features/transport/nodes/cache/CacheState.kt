package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.nodes.types.Node

sealed interface CacheState {
	data object Empty: CacheState
	data class Present(val node: Node): CacheState
}
