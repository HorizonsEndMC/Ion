package net.horizonsend.ion.server.features.transport.nodes.cache

sealed interface CacheState {
	data object Empty: CacheState
	data class Present(val node: CachedNode): CacheState
}
