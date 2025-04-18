package net.horizonsend.ion.server.features.transport.nodes.util

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

abstract class DestinationCache(protected val parentCache: TransportCache) {
	companion object {
		val EXPIRES_AFTER = TimeUnit.SECONDS.toMillis(15)
	}

	data class CachedDestinations(val cachTimestamp: Long, val destinations: ObjectOpenHashSet<PathfindingNodeWrapper>) {
		fun isExpired(): Boolean = (cachTimestamp + EXPIRES_AFTER) < System.currentTimeMillis()
	}

	abstract fun remove(nodeType: KClass<out Node>, origin: BlockKey)
	abstract fun invalidatePaths(nodeType: KClass<out Node>, pos: BlockKey, node: Node)
	abstract fun invalidatePaths(pos: BlockKey, node: Node)

	abstract fun clear()
}
