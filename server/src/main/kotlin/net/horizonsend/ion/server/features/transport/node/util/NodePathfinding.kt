package net.horizonsend.ion.server.features.transport.node.util

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.node.TransportNode
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
	val queue = ArrayDeque<PathfindingNodeWrapper>(1)
	queue.add(PathfindingNodeWrapper(from, null, 0, 0))

	val visited = ArrayDeque<PathfindingNodeWrapper>()

	var iterations = 0

	while (queue.isNotEmpty() && iterations < 150) {
		iterations++
		val current = queue.minBy { it.f }

		if (current.node == to) {
			return current.buildPath()
		}

		queue.remove(current)
		visited.add(current)

		for (neighbor in getNeighbors(current)) {
			if (visited.contains(neighbor)) continue
			neighbor.f = (neighbor.g + getHeuristic(neighbor, to))

			val existingNeighbor = queue.firstOrNull { it.node === neighbor.node }
			if (existingNeighbor != null) {
				if (neighbor.g < existingNeighbor.g) {
					existingNeighbor.g = neighbor.g
					existingNeighbor.parent = neighbor.parent
				}
			} else {
				queue.add(neighbor)
			}
		}
	}

	return null
}

// Wraps neighbor nodes in a data class to store G and F values for pathfinding. Should probably find a better solution
private fun getNeighbors(parent: PathfindingNodeWrapper): Array<PathfindingNodeWrapper> {
	val transferable = parent.node.cachedTransferable
	return Array(transferable.size) {
		val neighbor = transferable[it]

		PathfindingNodeWrapper(
			node = neighbor,
			parent = parent,
			g = parent.g + parent.node.getDistance(neighbor).roundToInt(),
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
