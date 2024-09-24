package net.horizonsend.ion.server.features.transport.node.util

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerPathfindingNode
import java.util.PriorityQueue
import kotlin.math.roundToInt

inline fun <reified T: TransportNode> getNetworkDestinations(origin: TransportNode, check: (T) -> Boolean): ObjectOpenHashSet<T> {
	val visitQueue = ArrayDeque<TransportNode>()
	val visitedSet = ObjectOpenHashSet<TransportNode>()
	val destinations = ObjectOpenHashSet<T>()

	visitQueue.addAll(origin.cachedTransferable)

	while (visitQueue.isNotEmpty()) {
		val currentNode = visitQueue.removeFirst()
		visitedSet.add(currentNode)

		if (currentNode is T && check(currentNode)) {
			destinations.add(currentNode)
		}

		visitQueue.addAll(currentNode.cachedTransferable.filterNot { visitedSet.contains(it) })
	}

	return destinations
}

/**
 * Uses the A* algorithm to find the shortest available path between these two nodes.
 **/
fun getIdealPath(from: TransportNode, to: TransportNode): Array<TransportNode>? {
	// There are 2 collections here. First the priority queue contains the next nodes, which needs to be quick to iterate.
	val queue = PriorityQueue<PathfindingNodeWrapper> { o1, o2 -> o2.f.compareTo(o1.f) }
	// The hash set here is to speed up the .contains() check further down the road, which is slow with the queue.
	val queueSet = IntOpenHashSet()

	fun queueAdd(wrapper: PathfindingNodeWrapper) {
		queue.add(wrapper)
		queueSet.add(wrapper.node.hashCode())
	}

	fun queueRemove(wrapper: PathfindingNodeWrapper) {
		queue.remove(wrapper)
		queueSet.remove(wrapper.node.hashCode())
	}

	queueAdd(PathfindingNodeWrapper(from, null, 0, 0))

	val visited = IntOpenHashSet()

	// Safeguard
	var iterations = 0

	while (queue.isNotEmpty() && iterations < 150) {
		iterations++
		val current = queue.minBy { it.f }

		if (current.node == to) return current.buildPath()

		queueRemove(current)
		visited.add(current.node.hashCode())

		for (neighbor in getNeighbors(current)) {
			if (visited.contains(neighbor.node.hashCode())) continue
			neighbor.f = (neighbor.g + getHeuristic(neighbor, to))

			if (queueSet.contains(neighbor.node.hashCode())) {
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
fun getNeighbors(parent: PathfindingNodeWrapper): Array<PathfindingNodeWrapper> {
	val parentParent = parent.parent
	val transferable = if (parentParent != null && parent.node is PowerPathfindingNode) {
		parent.node.getNextNodes(parentParent.node)
	} else parent.node.cachedTransferable

	return Array(transferable.size) {
		val neighbor = transferable[it]

		PathfindingNodeWrapper(
			node = neighbor,
			parent = parent,
			g = parent.g + parent.node.getDistance(neighbor).toInt(),
			f = 1
		)
	}
}

// The heuristic used for the algorithm in this case is the distance from the node to the destination, which is typical,
// But it also includes the pathfinding resistance to try to find the least resistant path.
fun getHeuristic(wrapper: PathfindingNodeWrapper, destination: TransportNode): Int {
	val resistance = wrapper.node.getPathfindingResistance(wrapper.parent?.node, null)
	return wrapper.node.getDistance(destination).roundToInt() + resistance
}

data class PathfindingNodeWrapper(val node: TransportNode, var parent: PathfindingNodeWrapper?, var g: Int, var f: Int) {
	// Compiles the path
	fun buildPath(): Array<TransportNode> {
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

fun calculatePathResistance(path: Array<TransportNode>?): Double? {
	if (path == null) return null

	var sum = 0.0

	for (index in path.indices) {
		val current = path[index]

		sum += current.getPathfindingResistance(
			previousNode = path.getOrNull(index - 1),
			nextNode = path.getOrNull(index + 1)
		)
	}

	return sum
}
