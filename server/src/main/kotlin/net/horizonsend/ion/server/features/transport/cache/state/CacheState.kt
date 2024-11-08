package net.horizonsend.ion.server.features.transport.cache.state

import net.horizonsend.ion.server.features.transport.cache.CachedNode

sealed interface CacheState {
	data object Empty: CacheState
	data class Present(val node: CachedNode): CacheState
}
