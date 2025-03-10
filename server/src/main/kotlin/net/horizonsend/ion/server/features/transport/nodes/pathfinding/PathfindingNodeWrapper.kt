package net.horizonsend.ion.server.features.transport.nodes.pathfinding

import net.horizonsend.ion.server.features.transport.nodes.types.Node

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
 	// Compiles the path.
	fun buildPath(): Array<Node.NodePositionData> {
		val list = arrayListOf(this.node)
		var current: PathfindingNodeWrapper? = this

		while (current?.parent != null) {
			current = current.parent!!
			list.add(current.node)
		}

		return list.toTypedArray()
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
