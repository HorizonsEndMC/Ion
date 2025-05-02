package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

data class Path(val length: Int, val trackedNodes: Array<Pair<BlockKey, Node>>): Iterable<Pair<BlockKey, Node>> {
	fun isValid(cache: CacheHolder<*>): Boolean {
		return all { (position, nodeType) ->
			cache.globalNodeLookup.invoke(cache.cache, cache.getWorld(), position)?.second == nodeType
		}
	}

	override fun iterator(): Iterator<Pair<BlockKey, Node>> {
		return trackedNodes.iterator()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Path

		if (length != other.length) return false
		if (!trackedNodes.contentEquals(other.trackedNodes)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = length
		result = 31 * result + trackedNodes.contentHashCode()
		return result
	}
}
