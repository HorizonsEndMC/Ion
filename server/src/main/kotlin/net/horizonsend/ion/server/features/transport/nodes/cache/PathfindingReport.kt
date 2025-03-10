package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.features.transport.nodes.types.Node

data class PathfindingReport(val traversedNodes: Array<Node.NodePositionData>, val resistance: Double) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PathfindingReport

		if (resistance != other.resistance) return false
		if (!traversedNodes.contentEquals(other.traversedNodes)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = resistance.hashCode()
		result = 31 * result + traversedNodes.contentHashCode()
		return result
	}
}
