package net.horizonsend.ion.server.features.transport.nodes.util

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class MonoDestinationCache(parentCache: TransportCache) : DestinationCache(parentCache) {
	private val rawCache: ConcurrentHashMap<KClass<out Node>, ConcurrentHashMap<BlockKey, CachedDestinations>> = ConcurrentHashMap()

	private fun getCache(nodeType: KClass<out Node>): ConcurrentHashMap<BlockKey, CachedDestinations> {
		return rawCache.getOrPut(nodeType) { ConcurrentHashMap() }
	}

	fun contains(nodeType: KClass<out Node>, origin: BlockKey): Boolean {
		return getCache(nodeType).containsKey(origin)
	}

	fun getOrPut(nodeType: KClass<out Node>, origin: BlockKey, cachingFunction: () -> Array<PathfindResult>?): Array<PathfindResult>? {
		val entries = get(nodeType, origin)
		if (entries != null) return entries

		val new = cachingFunction.invoke() ?: return null
		set(nodeType, origin, new)
		return new
	}

	fun get(nodeType: KClass<out Node>, origin: BlockKey): Array<PathfindResult>? {
		return getCache(nodeType)[origin]?.takeIf { !it.isExpired() }?.destinations
	}

	fun set(nodeType: KClass<out Node>, origin: BlockKey, value: Array<PathfindResult>) {
		getCache(nodeType)[origin] = CachedDestinations(System.currentTimeMillis(), value)
	}

	override fun remove(nodeType: KClass<out Node>, origin: BlockKey) {
		getCache(nodeType).remove(origin)?.destinations
	}

	override fun invalidatePaths(nodeType: KClass<out Node>, pos: BlockKey, node: Node) {
		val toRemove = LongOpenHashSet()

		if (contains(nodeType, pos)) {
			toRemove.add(pos)
		}

		// Perform a flood fill to find all network destinations, then remove all destination columns
		parentCache.getNetworkDestinations(destinationTypeClass = parentCache.extractorNodeClass, originPos = pos, originNode = node, retainFullPath = true) {
			// Traverse network backwards
			getAllNeighbors(cache.holder.globalNodeLookup, null)
		}.forEach { inputPos ->
			toRemove.add(inputPos.destinationPosition)
		}

		// Remove all the paths after being found
		for (key in toRemove.iterator()) {
			getCache(nodeType).remove(key)
		}

	}

	override fun invalidatePaths(pos: BlockKey, node: Node) {
		for (nodeClass in rawCache.keys) invalidatePaths(nodeClass, pos, node)
	}

	override fun clear() {
		rawCache.clear()
	}
}
