package net.horizonsend.ion.server.features.transport.nodes

import net.horizonsend.ion.server.features.transport.nodes.util.Path
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class PathfindResult(val destinationPosition: BlockKey, val trackedPath: Path): Comparable<PathfindResult> {
	override fun compareTo(other: PathfindResult): Int {
		return trackedPath.length.compareTo(trackedPath.length)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as PathfindResult

		if (destinationPosition != other.destinationPosition) return false
		if (trackedPath != other.trackedPath) return false

		return true
	}

	override fun hashCode(): Int {
		var result = destinationPosition.hashCode()
		result = 31 * result + trackedPath.hashCode()
		return result
	}
}
