package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

abstract class DestinationCache(protected val parentCache: TransportCache) {
	companion object {
		val EXPIRES_AFTER = TimeUnit.SECONDS.toMillis(25)
	}

	data class CachedDestinations(val cachTimestamp: Long, val destinations: Array<PathfindResult>) {
		fun isExpired(): Boolean = (cachTimestamp + EXPIRES_AFTER) < System.currentTimeMillis()

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as CachedDestinations

			if (cachTimestamp != other.cachTimestamp) return false
			if (!destinations.contentEquals(other.destinations)) return false

			return true
		}

		override fun hashCode(): Int {
			var result = cachTimestamp.hashCode()
			result = 31 * result + destinations.contentHashCode()
			return result
		}
	}

	abstract fun remove(nodeType: KClass<out Node>, origin: BlockKey)
	abstract fun invalidatePaths(nodeType: KClass<out Node>, pos: BlockKey, node: Node)
	abstract fun invalidatePaths(pos: BlockKey, node: Node)

	abstract fun clear()
}
