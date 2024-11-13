package net.horizonsend.ion.server.features.transport.util

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.util.PriorityQueue
import kotlin.math.roundToInt

fun getOrCacheNode(type: CacheType, world: World, pos: BlockKey): Node? {
	val chunk = IonChunk[world, getX(pos).shr(4), getZ(pos).shr(4)] ?: return null
	return type.get(chunk).getOrCache(pos)
}

inline fun <reified T: Node> getNetworkDestinations(cacheType: CacheType, world: World, originPos: BlockKey, check: (Node.NodePositionData) -> Boolean): List<BlockKey> {
	val originNode = getOrCacheNode(cacheType, world, originPos) ?: return listOf()

	val visitQueue = ArrayDeque<Node.NodePositionData>()
	val visited = LongOpenHashSet()
	val destinations = LongOpenHashSet()

	visitQueue.addAll(originNode.getNextNodes(
		world = world,
		position = originPos,
		backwards = BlockFace.SELF
	))

	while (visitQueue.isNotEmpty()) {
		val current = visitQueue.removeFirst()
		visited.add(current.position)

		if (current.type is T && check(current)) destinations.add(current.position)

		visitQueue.addAll(current.getNextNodes().filterNot { visited.contains(it.position) || visitQueue.contains(it) })
	}

	return destinations.toList()
}

/**
 * Uses the A* algorithm to find the shortest available path between these two nodes.
 **/
fun getIdealPath(from: Node.NodePositionData, to: BlockKey): Array<Node.NodePositionData>? {
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
		f = 0
	))

	val visited = IntOpenHashSet()

	// Safeguard
	var iterations = 0

	while (queue.isNotEmpty() && iterations < 150) {
		iterations++
		val current = queue.minBy { it.f }

		if (current.node.position == to) return current.buildPath()

		queueRemove(current)
		visited.add(current.node.hashCode())

		for (neighbor in getNeighbors(current)) {
			if (visited.contains(neighbor.node.hashCode())) continue
			neighbor.f = (neighbor.g + getHeuristic(neighbor, to))

			if (queueSet.contains(neighbor.node.position)) {
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
fun getNeighbors(current: PathfindingNodeWrapper): Array<PathfindingNodeWrapper> {
	val transferable = current.node.getNextNodes()

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
fun getHeuristic(wrapper: PathfindingNodeWrapper, destination: BlockKey): Int {
	val resistance = wrapper.node.type.pathfindingResistance
	return (toVec3i(wrapper.node.position).distance(toVec3i(destination)) + resistance).roundToInt()
}

/**
 * @param node The cached node at this position
 * @param parent The parent node
 **/
data class PathfindingNodeWrapper(
	val node: Node.NodePositionData,
	var parent: PathfindingNodeWrapper?,
	var g: Int,
	var f: Int
) {
 	// Compiles the path
	fun buildPath(): Array<Node.NodePositionData> {
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
		if (parent != other.parent) return false
		if (g != other.g) return false
		return f == other.f
	}

	override fun hashCode(): Int {
		var result = node.hashCode()
		result = 31 * result + (parent?.hashCode() ?: 0)
		result = 31 * result + g
		result = 31 * result + f
		return result
	}
	//</editor-fold>
}

fun calculatePathResistance(path: Array<Node.NodePositionData>?): Double? {
	return path?.sumOf { it.type.pathfindingResistance }
}
