package net.horizonsend.ion.server.features.transport.nodes.cache.path

import com.google.common.collect.TreeBasedTable
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache.PathfindingReport
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.set
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class PathCache<T : Any>(val cache: TransportCache) {
	private val pathCache = TreeBasedTable.create<BlockKey, BlockKey, Optional<T>>()

	private val lock = Any()

	fun contains(origin: BlockKey, destination: BlockKey): Boolean {
		return synchronized(lock) {
			pathCache.contains(origin, destination)
		}
	}

	fun get(origin: BlockKey, destination: BlockKey): T? {
		return synchronized(lock) {
			pathCache.get(origin, destination)?.getOrNull()
		}
	}

	fun set(origin: BlockKey, destination: BlockKey, value: T?) {
		synchronized(lock) {
			pathCache[origin, destination] = Optional.ofNullable(value)
		}
	}

	fun remove(origin: BlockKey, destination: BlockKey): T? {
		return synchronized(lock) {
			pathCache.remove(origin, destination)?.getOrNull()
		}
	}

	fun getOrCompute(origin: BlockKey, destination: BlockKey, provier: () -> T?): T? {
		if (contains(origin, destination)) return get(origin, destination)

		val result = provier()
		set(origin, destination, result)
		return result
	}

	fun invalidatePaths(pos: BlockKey, node: Node, cacheNewNodes: Boolean = false) {
		val toRemove = mutableSetOf<Pair<BlockKey, BlockKey>>()

		// If the pos is an origin or destination, there is no need to perform a flood to find the connected rows / destinations
		// If the path cache contains a row at this pos, an origin is present here, and it can be removed
		if (pathCache.containsRow(pos)) {
			pathCache.rowMap()[pos]?.keys?.forEach { columnKey ->
				toRemove.add(pos to columnKey)
			}
		}

		// If the path cache contains a column at this pos, a destination from an origin within the chunk can be invalidated
		if (pathCache.containsColumn(pos)) {
			pathCache.columnMap()[pos]?.keys?.forEach { rowKey ->
				toRemove.add(rowKey to pos)
			}
		}

		// Perform a flood fill to find all network destinations, then remove all destination columns
		cache.getNetworkDestinations(clazz = cache.extractorNodeClass, originPos = pos, originNode = node) {
			// Traverse network backwards
			getPreviousNodes(cache.holder.nodeProvider, null)
		}.forEach { extractorPos ->
			pathCache.rowMap()[extractorPos]?.keys?.forEach { columnKey ->
				toRemove.add(extractorPos to columnKey)
			}
		}

		// Remove all the paths after being found
		synchronized(lock) {
			for ((rowKey, columnKey) in toRemove) pathCache.remove(rowKey, columnKey)
		}
	}

	companion object {
		fun standard(cache: TransportCache): PathCache<PathfindingReport> = PathCache(cache)
		fun <T: Any> keyed(cache: TransportCache): PathCache<MutableMap<T, Optional<PathfindingReport>>> = PathCache(cache)
	}
}
