package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.types.Node

/**
 * @param node The cached node at this position
 * @param parent The parent node
 **/
class PathfindingNodeWrapper(val node: Node.NodePositionData, parent: PathfindingNodeWrapper?) : Comparable<PathfindingNodeWrapper> {
	var depth: Int = (parent?.depth ?: 0) + 1

	var parent: PathfindingNodeWrapper? = parent
		set(value) {
			field = value
			depth = (value?.depth ?: 0) + 1
		}

	private var cachedPath: Path? = null

	fun buildPath(): Path {
		cachedPath?.let { return it }

		val list = arrayListOf(this.node)
		var current: PathfindingNodeWrapper? = this

		while (current?.parent != null) {
			current = current.parent!!
			list.add(current.node)
		}

		val path = Path(list.toTypedArray())
		cachedPath = path

		return path
	}

	override fun compareTo(other: PathfindingNodeWrapper): Int {
		return depth.compareTo(other.depth)
	}
}
