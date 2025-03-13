package net.horizonsend.ion.server.features.transport.nodes.util

import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData

data class Path(val nodes: Array<NodePositionData>): Iterable<NodePositionData> {
	fun isValid(): Boolean {
		return all { it.cache.getCached(it.position) == it.type }
	}

	override fun iterator(): Iterator<NodePositionData> {
		return nodes.iterator()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Path

		return nodes.contentEquals(other.nodes)
	}

	override fun hashCode(): Int {
		return nodes.contentHashCode()
	}
}
