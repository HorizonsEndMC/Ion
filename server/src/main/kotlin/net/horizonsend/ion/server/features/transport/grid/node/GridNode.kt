package net.horizonsend.ion.server.features.transport.grid.node

import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

interface GridNode {
	val parentGrid: Grid
	val x: Int
	val y: Int
	val z: Int

	val key get() = toBlockKey(x, y, z)

	val neighbors: ConcurrentHashMap<BlockFace, GridNode>

	/**
	 * Collects the neighbors of this node
	 **/
	fun collectNeighbors() {
		for (direction in ADJACENT_BLOCK_FACES) {
			val newX = x + direction.modX
			val newY = y + direction.modY
			val newZ = z + direction.modZ

			val possibleNode = parentGrid.getNode(newX, newY, newZ) ?: continue

			neighbors[direction] = possibleNode
		}
	}

	fun getNeighbor(face: BlockFace): GridNode? {
		return neighbors[face]
	}

	/**
	 * Replace this node with another and update its neighbors
	 **/
	fun replace(new: GridNode) {
		for ((offset, neighbor) in neighbors) {
			val neighborRelation = offset.oppositeFace

			neighbor.neighbors[neighborRelation] = neighbor
		}

		parentGrid.nodes[key] = new
	}
}
