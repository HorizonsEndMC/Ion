package net.horizonsend.ion.server.features.transport.nodes.cache.util

import com.google.common.collect.HashBasedTable
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache.PathfindingReport
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.set
import java.util.Optional
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.jvm.optionals.getOrNull

class PathCache<T : Any>(val cache: TransportCache) {
	private val pathCache = HashBasedTable.create<BlockKey, BlockKey, Optional<T>>()

	private val lock = ReentrantReadWriteLock(true)

	fun contains(origin: BlockKey, destination: BlockKey): Boolean {
		lock.readLock().lock()
		try {
			return pathCache.contains(origin, destination)
		} finally {
			lock.readLock().unlock()
		}
	}

	fun get(origin: BlockKey, destination: BlockKey): T? {
		lock.readLock().lock()
		try {
			return pathCache.get(origin, destination)?.getOrNull()
		} finally {
			lock.readLock().unlock()
		}
	}

	fun set(origin: BlockKey, destination: BlockKey, value: T?) {
		lock.writeLock().lock()
		try {
			pathCache[origin, destination] = Optional.ofNullable(value)
		} finally {
			lock.writeLock().unlock()
		}
	}

	fun remove(origin: BlockKey, destination: BlockKey): T? {
		lock.writeLock().lock()
		try {
			return pathCache.remove(origin, destination)?.getOrNull()
		} finally {
			lock.writeLock().unlock()
		}
	}

	fun getOrCompute(origin: BlockKey, destination: BlockKey, provier: () -> T?): T? {
		if (contains(origin, destination)) return get(origin, destination)

		val result = provier()
		set(origin, destination, result)
		return result
	}

	fun containsOriginPoint(origin: BlockKey): Boolean {
		lock.readLock().lock()
		try {
			return pathCache.containsRow(origin)
		} finally {
			lock.readLock().unlock()
		}
	}

	fun containsDestinationPoint(destination: BlockKey): Boolean {
		lock.readLock().lock()
		try {
			return pathCache.containsColumn(destination)
		} finally {
			lock.readLock().unlock()
		}
	}

	fun invalidatePaths(pos: BlockKey, node: Node, cacheNewNodes: Boolean = false) {
		val toRemove = mutableSetOf<Pair<BlockKey, BlockKey>>()

		// If the pos is an origin or destination, there is no need to perform a flood to find the connected rows / destinations
		// If the path cache contains a row at this pos, an origin is present here, and it can be removed
		if (containsOriginPoint(pos)) {
			pathCache.rowMap()[pos]?.keys?.forEach { columnKey ->
				toRemove.add(pos to columnKey)
			}
		}

		// If the path cache contains a column at this pos, a destination from an origin within the chunk can be invalidated
		if (containsDestinationPoint(pos)) {
			pathCache.columnMap()[pos]?.keys?.forEach { rowKey ->
				toRemove.add(rowKey to pos)
			}
		}

		val nodeProvider = if (cacheNewNodes) cache.holder.nodeCacherGetter else cache.holder.cachedNodeLookup

		// Perform a flood fill to find all network destinations, then remove all destination columns
		cache.getNetworkDestinations(clazz = cache.extractorNodeClass, originPos = pos, originNode = node) {
			// Traverse network backwards
			getPreviousNodes(nodeProvider, null)
		}.forEach { extractorPos ->
			pathCache.rowMap()[extractorPos]?.keys?.forEach { columnKey -> toRemove.add(extractorPos to columnKey) }
		}

		// Remove all the paths after being found
		lock.writeLock().lock()
		try {
			for ((rowKey, columnKey) in toRemove) {
				pathCache.remove(rowKey, columnKey)
			}
		} finally {
			lock.writeLock().unlock()
		}
	}

	companion object {
		fun standard(cache: TransportCache): PathCache<PathfindingReport> = PathCache(cache)
		fun <T: Any> keyed(cache: TransportCache): PathCache<MutableMap<T, Optional<PathfindingReport>>> = PathCache(cache)
	}
}
