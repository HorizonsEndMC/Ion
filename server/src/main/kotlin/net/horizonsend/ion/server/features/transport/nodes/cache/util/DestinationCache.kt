package net.horizonsend.ion.server.features.transport.nodes.cache.util

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.reflect.KClass

class DestinationCache(val cache: TransportCache) {
	private val pathCache: Object2ObjectOpenHashMap<KClass<out Node>, Long2ObjectOpenHashMap<LongOpenHashSet>> = Object2ObjectOpenHashMap()

	private val lock = ReentrantReadWriteLock(true)

	private fun getCache(nodeType: KClass<out Node>): Long2ObjectOpenHashMap<LongOpenHashSet> {
		return pathCache.getOrPut(nodeType) { Long2ObjectOpenHashMap() }
	}

	fun contains(nodeType: KClass<out Node>, origin: BlockKey): Boolean {
		lock.readLock().lock()
		try {
			return getCache(nodeType).containsKey(origin)
		} finally {
			lock.readLock().unlock()
		}
	}

	fun get(nodeType: KClass<out Node>, origin: BlockKey): Set<Long> {
		lock.readLock().lock()
		try {
			return getCache(nodeType).getOrDefault(origin, setOf())
		} finally {
			lock.readLock().unlock()
		}
	}

	fun set(nodeType: KClass<out Node>, origin: BlockKey, value: Set<Long>) {
		lock.writeLock().lock()
		try {
			getCache(nodeType)[origin] = LongOpenHashSet(value)
		} finally {
			lock.writeLock().unlock()
		}
	}

	fun remove(nodeType: KClass<out Node>, origin: BlockKey): Set<Long> {
		lock.writeLock().lock()
		try {
			return getCache(nodeType).remove(origin)
		} finally {
			lock.writeLock().unlock()
		}
	}

	fun getOrCompute(nodeType: KClass<out Node>, origin: BlockKey, provier: () -> Set<Long>): Set<Long> {
		if (contains(nodeType, origin)) return get(nodeType, origin)

		val result = provier()
		set(nodeType, origin, result)
		return result
	}

	fun invalidatePaths(nodeType: KClass<out Node>, pos: BlockKey, node: Node) {
		val toRemove = LongOpenHashSet()

		if (contains(nodeType, pos)) {
			toRemove.add(pos)
		}

		// Perform a flood fill to find all network destinations, then remove all destination columns
		cache.getNetworkDestinations(clazz = cache.extractorNodeClass, originPos = pos, originNode = node) {
			// Traverse network backwards
			getPreviousNodes(cache.holder.cachedNodeLookup, null)
		}.forEach { extractorPos ->
			toRemove.add(extractorPos)
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

	fun invalidatePaths(pos: BlockKey, node: Node) {
		for (nodeClass in pathCache.keys) invalidatePaths(nodeClass, pos, node)
	}
}
