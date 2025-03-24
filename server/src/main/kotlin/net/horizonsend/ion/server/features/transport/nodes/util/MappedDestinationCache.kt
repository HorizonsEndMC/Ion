package net.horizonsend.ion.server.features.transport.nodes.util

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class MappedDestinationCache<K : Any>(parentCache: TransportCache) : DestinationCache(parentCache) {
	private val rawCache: ConcurrentHashMap<KClass<out Node>, ConcurrentHashMap<K, ConcurrentHashMap<BlockKey, CachedDestinations>>> = ConcurrentHashMap()

	private fun getCache(nodeType: KClass<out Node>, mapKey: K): ConcurrentHashMap<BlockKey, CachedDestinations> {
		return rawCache.computeIfAbsent(nodeType) { ConcurrentHashMap() }.computeIfAbsent(mapKey) { ConcurrentHashMap() }
	}

	private fun getCache(nodeType: KClass<out Node>): ConcurrentHashMap<K, ConcurrentHashMap<BlockKey, CachedDestinations>> {
		return rawCache.computeIfAbsent(nodeType) { ConcurrentHashMap() }
	}

	fun contains(nodeType: KClass<out Node>, mapKey: K, origin: BlockKey): Boolean {
		return getCache(nodeType, mapKey).containsKey(origin)
	}

	fun contains(nodeType: KClass<out Node>, origin: BlockKey): Boolean {
		return getCache(nodeType).values.any { it.containsKey(origin) }
	}

	fun getOrPut(nodeType: KClass<out Node>, mapKey: K, origin: BlockKey, cachingFunction: () -> Set<PathfindingNodeWrapper>?): Set<PathfindingNodeWrapper>? {
		val entries = get(nodeType, mapKey, origin)
		if (entries != null) return entries

		val new = cachingFunction.invoke() ?: return null
		set(nodeType, mapKey, origin, new)
		return new
	}

	fun get(nodeType: KClass<out Node>, mapKey: K, origin: BlockKey): Set<PathfindingNodeWrapper>? {
		return getCache(nodeType, mapKey)[origin]?.takeIf { !it.isExpired() }?.destinations
	}

	fun set(nodeType: KClass<out Node>, mapKey: K, origin: BlockKey, value: Set<PathfindingNodeWrapper>) {
		getCache(nodeType, mapKey)[origin] = CachedDestinations(System.currentTimeMillis(), ObjectOpenHashSet(value))
	}

	override fun remove(nodeType: KClass<out Node>, origin: BlockKey) {
		val rawNodeCache = getCache(nodeType)
		rawNodeCache.keys.forEach { key -> rawNodeCache[key]?.remove(origin) }
	}

	override fun invalidatePaths(nodeType: KClass<out Node>, pos: BlockKey, node: Node) {
		val toRemove = LongOpenHashSet()

		if (contains(nodeType, pos)) {
			toRemove.add(pos)
		}

		// Perform a flood fill to find all network destinations, then remove all destination columns
		parentCache.getNetworkDestinations(destinationTypeClass = parentCache.extractorNodeClass, originPos = pos, originNode = node) {
			// Traverse network backwards
			getAllNeighbors(cache.holder.globalCacherGetter, null)
		}.forEach { inputPos ->
			toRemove.add(inputPos.node.position)
		}

		// Remove all the paths after being found
		for (removePos in toRemove.iterator()) {
			val rawNodeCache = getCache(nodeType)
			rawNodeCache.keys.forEach { key -> rawNodeCache[key]?.remove(removePos) }
		}
	}

	override fun invalidatePaths(pos: BlockKey, node: Node) {
		for (nodeClass in rawCache.keys) invalidatePaths(nodeClass, pos, node)
	}
}
