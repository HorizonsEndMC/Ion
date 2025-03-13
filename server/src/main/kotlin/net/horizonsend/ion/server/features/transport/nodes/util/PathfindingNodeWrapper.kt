package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.types.Node

/**
 * @param node The cached node at this position
 * @param parent The parent node
 **/
data class PathfindingNodeWrapper(
	val node: Node.NodePositionData,
	var parent: PathfindingNodeWrapper?
) {
	fun buildPath(): Path {
		val list = arrayListOf(this.node)
		var current: PathfindingNodeWrapper? = this

		while (current?.parent != null) {
			current = current.parent!!
			list.add(current.node)
		}

		return Path(list.toTypedArray())
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PathfindingNodeWrapper

		if (node != other.node) return false
		if (parent != other.parent) return false

		return true
	}

	override fun hashCode(): Int {
		var result = node.hashCode()
		result = 31 * result + (parent?.hashCode() ?: 0)
		return result
	}
}
