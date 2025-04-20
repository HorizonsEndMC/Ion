package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.TrackedNode

/**
 * @param node The cached node at this position
 * @param parent The parent node
 **/
class PathfindingNodeWrapper(val node: Node.NodePositionData, parent: PathfindingNodeWrapper?) : Comparable<PathfindingNodeWrapper> {
	private var depth: Int = (parent?.depth ?: 0) + 1

	var parent: PathfindingNodeWrapper? = parent
		set(value) {
			field = value
			depth = (value?.depth ?: 0) + 1
		}

	fun buildPath(retainfull: Boolean): Path {
		val list = arrayListOf(this.node.position to this.node.type)
		var current: PathfindingNodeWrapper? = this

		while (current?.parent != null) {
			current = current.parent!!

			if (retainfull || node.type is TrackedNode) {
				list.add(current.node.position to current.node.type)
			}
		}

		return Path(depth, list.toTypedArray())
	}

	override fun compareTo(other: PathfindingNodeWrapper): Int {
		return depth.compareTo(other.depth)
	}
}
