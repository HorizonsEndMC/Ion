package net.horizonsend.ion.server.features.transport.nodes.pathfinding

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.transport.manager.holders.CacheProvider
import net.horizonsend.ion.server.features.transport.manager.holders.ChunkCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.util.PriorityQueue
import kotlin.math.roundToInt

fun getOrCacheNode(currentCache: TransportCache, type: CacheType, world: World, pos: BlockKey): Pair<TransportCache, Node?>? {
	val holder = currentCache.holder
	if (
		holder is ChunkCacheHolder<*>  &&
		getX(pos).shr(4) == holder.transportManager.chunk.x &&
		getZ(pos).shr(4) == holder.transportManager.chunk.z
	) return holder.cache to holder.cache.getOrCache(pos)

	val chunk = IonChunk[world, getX(pos).shr(4), getZ(pos).shr(4)] ?: return null
	val cache = type.get(chunk)
	return cache to cache.getOrCache(pos)
}

fun getGlobalNode(currentCache: TransportCache, type: CacheType, world: World, pos: BlockKey): Pair<TransportCache, Node?>? {
	val holder = currentCache.holder
	if (
		holder is ChunkCacheHolder<*>  &&
		getX(pos).shr(4) == holder.transportManager.chunk.x &&
		getZ(pos).shr(4) == holder.transportManager.chunk.z
	) return holder.cache to holder.cache.getCached(pos)

	val chunk = IonChunk[world, getX(pos).shr(4), getZ(pos).shr(4)] ?: return null
	val cache = type.get(chunk)
	return cache to cache.getCached(pos)
}

/**
 * Uses the A* algorithm to find the shortest available path between these two nodes.
 **/
fun getIdealPath(
	from: Node.NodePositionData,
	destination: BlockKey,
	pathTracker: PathTracker?,
	cachedNodeProvider: CacheProvider,
	pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null
): Array<Node.NodePositionData>? {
	// There are 2 collections here. First the priority queue contains the next nodes, which needs to be quick to iterate.
	val queue = PriorityQueue<PathfindingNodeWrapper> { o1, o2 -> o2.f.compareTo(o1.f) }
	// The hash set here is to speed up the .contains() check further down the road, which is slow with the queue.
	val queueSet = LongOpenHashSet()

	fun queueAdd(wrapper: PathfindingNodeWrapper) {
		queue.add(wrapper)
		queueSet.add(wrapper.node.position)
	}

	fun queueRemove(wrapper: PathfindingNodeWrapper) {
		queue.remove(wrapper)
		queueSet.remove(wrapper.node.position)
	}

	queueAdd(PathfindingNodeWrapper(
		node = from,
		parent = null,
		g = 0,
		f = getHeuristic(from, destination)
	))

	val visited = Long2IntOpenHashMap()

	fun markVisited(node: PathfindingNodeWrapper) {
		val pos = node.node.position
		val existing = visited.getOrDefault(pos, 0)

		visited[pos] = existing + 1
	}

	fun canVisit(node: Node.NodePositionData): Boolean {
		return visited.getOrDefault(node.position, 0) < node.type.getMaxPathfinds()
	}

	// Safeguard
	var iterations = 0

	val maxDepth = ConfigurationFiles.transportSettings().powerConfiguration.maxPathfindDepth
	while (queue.isNotEmpty() && iterations < maxDepth) {
		iterations++
		val current = queue.minBy { it.f }

		if (current.node.position == destination) {
			val path = current.buildPath()
			// Add the path to all positions along it
			pathTracker?.addPathLookup(path, path)

			return path
		}

		if (pathTracker != null) {
			val data = pathTracker.checkPosition(current)
			if (data != null) {
				return data
			}
		}

		queueRemove(current)
		markVisited(current)

		// Compute new neighbor data from current position
		for (computedNeighbor in getNeighbors(current, cachedNodeProvider, pathfindingFilter)) {
			if (!canVisit(computedNeighbor.node)) {
				continue
			}

			// Update the f value
			computedNeighbor.f = (computedNeighbor.g + getHeuristic(computedNeighbor.node, destination))

			if (queueSet.contains(computedNeighbor.node.position)) {
				val existingNeighbor = queue.first { it.node.position == computedNeighbor.node.position }

				if (computedNeighbor.g < existingNeighbor.g) {
					existingNeighbor.parent = computedNeighbor.parent

					existingNeighbor.g = computedNeighbor.g
					existingNeighbor.f = computedNeighbor.f
				}
			} else {
				queueAdd(computedNeighbor)
			}
		}
	}

	return null
}

// Wraps neighbor nodes in a data class to store G and F values for pathfinding. Should probably find a better solution
fun getNeighbors(current: PathfindingNodeWrapper, cachedNodeProvider: CacheProvider, filter: ((Node, BlockFace) -> Boolean)?): Array<PathfindingNodeWrapper> {
	val transferable = current.node.getPreviousNodes(cachedNodeProvider, filter)

	return Array(transferable.size) {
		val next = transferable[it]

		PathfindingNodeWrapper(
			node = next,
			parent = current,
			g = current.g + 1,
			f = 1
		)
	}
}

// The heuristic used for the algorithm in this case is the distance from the node to the destination, which is typical,
// But it also includes the pathfinding resistance to try to find the least resistant path.
fun getHeuristic(node: Node.NodePositionData, destination: BlockKey): Int {
	val resistance = node.type.pathfindingResistance
	return (toVec3i(node.position).distance(toVec3i(destination)) + resistance).roundToInt()
}

fun calculatePathResistance(path: Array<Node.NodePositionData>): Double {
	return path.sumOf { it.type.pathfindingResistance }
}

