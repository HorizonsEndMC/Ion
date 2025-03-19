package net.horizonsend.ion.server.features.transport.nodes.util

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

class MonoDestinationCache(parentCache: TransportCache) : DestinationCache(parentCache) {
	private val rawCache: Object2ObjectOpenHashMap<KClass<out Node>, Long2ObjectOpenHashMap<CachedDestinations>> = Object2ObjectOpenHashMap()

	private val lock = ReentrantReadWriteLock(true)

	private fun getCache(nodeType: KClass<out Node>): Long2ObjectOpenHashMap<CachedDestinations> {
		return rawCache.getOrPut(nodeType) { Long2ObjectOpenHashMap() }
	}

	fun contains(nodeType: KClass<out Node>, origin: BlockKey): Boolean {
		lock.readLock().lock()
		try {
			return getCache(nodeType).containsKey(origin)
		} finally {
			lock.readLock().unlock()
		}
	}

	fun getOrPut(nodeType: KClass<out Node>, origin: BlockKey, cachingFunction: () -> Set<PathfindingNodeWrapper>?): Set<PathfindingNodeWrapper>? {
		val entries = get(nodeType, origin)
		if (entries != null) return entries

		val new = cachingFunction.invoke() ?: return null
		set(nodeType, origin, new)
		return new
	}

	fun get(nodeType: KClass<out Node>, origin: BlockKey): Set<PathfindingNodeWrapper>? {
		return lock.readLock().withLock { getCache(nodeType)[origin] }?.takeIf { !it.isExpired() }?.destinations
	}

	fun set(nodeType: KClass<out Node>, origin: BlockKey, value: Set<PathfindingNodeWrapper>) {
		lock.writeLock().lock()
		try {
			getCache(nodeType)[origin] = CachedDestinations(System.currentTimeMillis(), ObjectOpenHashSet(value))
		} finally {
			lock.writeLock().unlock()
		}
	}

	override fun remove(nodeType: KClass<out Node>, origin: BlockKey) {
		lock.writeLock().lock()
		try {
			getCache(nodeType).remove(origin)?.destinations
		} finally {
			lock.writeLock().unlock()
		}
	}

	override fun invalidatePaths(nodeType: KClass<out Node>, pos: BlockKey, node: Node) {
		val toRemove = LongOpenHashSet()

		if (contains(nodeType, pos)) {
			toRemove.add(pos)
		}

		// Perform a flood fill to find all network destinations, then remove all destination columns
		parentCache.getNetworkDestinations(destinationTypeClass = parentCache.extractorNodeClass, originPos = pos, originNode = node) {
			// Traverse network backwards
			getPreviousNodes(cache.holder.globalCacherGetter, null)
		}.forEach { inputPos ->
			toRemove.add(inputPos.node.position)
		}

		// Remove all the paths after being found
		lock.writeLock().lock()
		try {
			for (key in toRemove.iterator()) {
				getCache(nodeType).remove(key)
			}
		} finally {
			lock.writeLock().unlock()
		}
	}

	override fun invalidatePaths(pos: BlockKey, node: Node) {
		for (nodeClass in rawCache.keys) invalidatePaths(nodeClass, pos, node)
	}
}
