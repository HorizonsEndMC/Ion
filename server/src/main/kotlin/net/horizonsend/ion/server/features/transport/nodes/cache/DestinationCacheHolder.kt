package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.nodes.util.DestinationCache

interface DestinationCacheHolder {
	val destinationCache: DestinationCache
}
