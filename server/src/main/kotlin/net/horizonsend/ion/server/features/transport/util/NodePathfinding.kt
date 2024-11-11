package net.horizonsend.ion.server.features.transport.util

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.transport.nodes.cache.CachedNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.audience.Audience
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.util.PriorityQueue
import kotlin.math.roundToInt

fun getOrCacheNode(type: NetworkType, world: World, pos: BlockKey): CachedNode? {
	val chunk = IonChunk[world, getX(pos).shr(4), getZ(pos).shr(4)] ?: return null
	return type.get(chunk).getOrCache(pos)
}

fun getNextNodes(networkType: NetworkType, world: World, backwards: BlockFace, parentPos: BlockKey, parentType: CachedNode): Map<BlockFace, CachedNode> {
	val adjacent = ADJACENT_BLOCK_FACES.minus(backwards)

	val map = mutableMapOf<BlockFace, CachedNode>()

	for (adjacentFace in adjacent) {
		val pos = getRelative(parentPos, adjacentFace)
		val cached = getOrCacheNode(networkType, world, pos) ?: continue

		if (!cached.canTransferFrom(parentType, adjacentFace) || !parentType.canTransferTo(cached, adjacentFace)) continue

		map[adjacentFace] = cached
	}

	return map
}

inline fun <reified T: CachedNode> getNetworkDestinations(world: World, networkType: NetworkType, originPos: BlockKey, check: (T) -> Boolean): LongOpenHashSet {
	val originNode = getOrCacheNode(networkType, world, originPos) ?: return LongOpenHashSet()

	val visitQueue = ArrayDeque<Triple<BlockFace, BlockKey, CachedNode>>()
	val visitedSet = LongOpenHashSet()
	val destinations = LongOpenHashSet()

	val immediate = getNextNodes(
		networkType = networkType,
		world = world,
		backwards = BlockFace.SELF,
		parentPos = originPos,
		parentType = originNode
	)

	visitQueue.addAll(immediate.map { Triple(it.key, getRelative(originPos, it.key), it.value) })

	while (visitQueue.isNotEmpty()) {
		val (offset, currentPos, currentNode) = visitQueue.removeFirst()
		visitedSet.add(currentPos)

		if (currentNode is T && check(currentNode)) {
			destinations.add(currentPos)
		}

		val next = getNextNodes(
			networkType = networkType,
			world = world,
			backwards = offset.oppositeFace,
			parentPos = currentPos,
			parentType = currentNode
		).filterNot { visitedSet.contains(getRelative(currentPos, it.key)) }.map { Triple(it.key, getRelative(originPos, it.key), it.value) }

		visitQueue.addAll(next)
	}

	return destinations
}

/**
 * Uses the A* algorithm to find the shortest available path between these two nodes.
 **/
fun getIdealPath(world: World, type: NetworkType, fromType: CachedNode, fromPos: BlockKey, to: BlockKey): Array<CachedNode>? {
	// There are 2 collections here. First the priority queue contains the next nodes, which needs to be quick to iterate.
	val queue = PriorityQueue<PathfindingNodeWrapper> { o1, o2 -> o2.f.compareTo(o1.f) }
	// The hash set here is to speed up the .contains() check further down the road, which is slow with the queue.
	val queueSet = LongOpenHashSet()

	fun queueAdd(wrapper: PathfindingNodeWrapper) {
		queue.add(wrapper)
		queueSet.add(wrapper.pos)
	}

	fun queueRemove(wrapper: PathfindingNodeWrapper) {
		queue.remove(wrapper)
		queueSet.remove(wrapper.pos)
	}

	queueAdd(
		PathfindingNodeWrapper(
		world = world,
		pos = fromPos,
		node = fromType,
		parent = null,
		offset = BlockFace.SELF,
		type = type,
		g = 0,
		f = 0
	)
	)

	val visited = IntOpenHashSet()

	// Safeguard
	var iterations = 0

	while (queue.isNotEmpty() && iterations < 150) {
		iterations++
		val current = queue.minBy { it.f }

		if (current.pos == to) return current.buildPath()

		queueRemove(current)
		visited.add(current.node.hashCode())

		for (neighbor in getNeighbors(current)) {
			if (visited.contains(neighbor.node.hashCode())) continue
			neighbor.f = (neighbor.g + getHeuristic(neighbor, to))

			if (queueSet.contains(neighbor.pos)) {
				val existingNeighbor = queue.first { it.node === neighbor.node }
				if (neighbor.g < existingNeighbor.g) {
					existingNeighbor.g = neighbor.g
					existingNeighbor.parent = neighbor.parent
				}
			} else {
				queueAdd(neighbor)
			}
		}
	}

	return null
}

// Wraps neighbor nodes in a data class to store G and F values for pathfinding. Should probably find a better solution
fun getNeighbors(current: PathfindingNodeWrapper, audience: Audience? = null): Array<PathfindingNodeWrapper> {
	val transferable = getNextNodes(
		current.type,
		current.world,
		backwards = current.offset.oppositeFace,
		parentPos = current.pos,
		parentType = current.node
	).toList()

	audience?.information("${transferable.size} transferable nodes")

	return Array(transferable.size) {
		val (neighborFace, neighborType) = transferable[it]
		val neighborPos = getRelative(current.pos, neighborFace)

		PathfindingNodeWrapper(
			world = current.world,
			pos = neighborPos,
			node = neighborType,
			parent = current,
			type = current.type,
			offset = neighborFace,
			g = current.g + 1,
			f = 1
		)
	}
}

// The heuristic used for the algorithm in this case is the distance from the node to the destination, which is typical,
// But it also includes the pathfinding resistance to try to find the least resistant path.
fun getHeuristic(wrapper: PathfindingNodeWrapper, destination: BlockKey): Int {
	val resistance = wrapper.node.pathfindingResistance
	return (toVec3i(wrapper.pos).distance(toVec3i(destination)) + resistance).roundToInt()
}

/**
 * @param pos The position of this node in the path
 * @param node The cached node at this position
 * @param parent The parent node
 * @param offset The offset direction between the parent and this node
 **/
data class PathfindingNodeWrapper(
	val world: World,
	val pos: BlockKey,
	val node: CachedNode,
	val type: NetworkType,
	var parent: PathfindingNodeWrapper?,
	var offset: BlockFace,
	var g: Int,
	var f: Int
) {
	val network get() = IonChunk.getFromWorldCoordinates(world, getX(pos), getZ(pos))?.let { type.get(it) }

 	// Compiles the path
	fun buildPath(): Array<CachedNode> {
		val list = mutableListOf(this.node)
		var current: PathfindingNodeWrapper? = this

		while (current?.parent != null) {
			current = current.parent!!
			list.add(current.node)
		}

		// Return a reversed array, to put the origin node at the head of the array
		val lastIndex = list.lastIndex
		return Array(list.size) {
			list[lastIndex - it]
		}
	}

	//<editor-fold desc="Generated Methods">
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PathfindingNodeWrapper

		if (node != other.node) return false
		if (g != other.g) return false
		return f == other.f
	}

	override fun hashCode(): Int {
		var result = node.hashCode()
		result = 31 * result + g
		result = 31 * result + f
		return result
	}
	//</editor-fold>
}

fun calculatePathResistance(path: Array<CachedNode>?): Double? {
	return path?.sumOf { it.pathfindingResistance }
}
