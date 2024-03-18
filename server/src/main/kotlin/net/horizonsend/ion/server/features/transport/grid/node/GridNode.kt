package net.horizonsend.ion.server.features.transport.grid.node

import net.horizonsend.ion.server.features.transport.grid.AbstractGrid
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

interface GridNode {
	val parent: AbstractGrid
	val x: Int
	val y: Int
	val z: Int

	val key get() = toBlockKey(x, y, z)

	val neighbors: ConcurrentHashMap<BlockFace, GridNode>

	fun getNeighbor(face: BlockFace): GridNode? {
		return neighbors[face]
	}

	/**
	 * Consolidates this node if possible
	 **/
	fun consolidate()

	/**
	 * Replace this node with another and update its neighbors
	 **/
	fun replace(new: GridNode) {
		for ((offset, neighbor) in neighbors) {
			val neighborRelation = offset.oppositeFace

			neighbor.neighbors[neighborRelation] = neighbor
		}
	}
}
