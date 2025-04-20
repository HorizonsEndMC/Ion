package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.TrackedNode

/**
 * @param node The cached node at this position
 * @param parent The parent node
 **/
class PathfindingNodeWrapper(val node: Node.NodePositionData, parent: PathfindingNodeWrapper?) : Comparable<PathfindingNodeWrapper> {
	private var depth: Int = (parent?.depth ?: 0) + 1
	var interestingPath: Boolean = node.type is TrackedNode || (parent != null && parent.interestingPath)

	var parent: PathfindingNodeWrapper? = parent
		set(value) {
			field = value
			depth = (value?.depth ?: 0) + 1
		}

	/**
	 * @param retainfull If true, build a full path of every node pathed over, if false the list will only contain tracked nodes.
	 **/
	fun buildPath(retainfull: Boolean): Path {
		// No tracked nodes on path, no need to compute the whole thing if the full path isn't requested
		if (!interestingPath && !retainfull) {
			return Path(depth, arrayOf())
		}

		val list = arrayListOf(this.node.position to this.node.type)
		var current: PathfindingNodeWrapper? = this

		// Iterate through each node's parents
		while (current?.parent != null) {
			current = current.parent!!

			if (retainfull || current.node.type is TrackedNode) {
				list.add(current.node.position to current.node.type)
			}
		}

		return Path(depth, list.toTypedArray())
	}

	override fun compareTo(other: PathfindingNodeWrapper): Int {
		return depth.compareTo(other.depth)
	}
}
