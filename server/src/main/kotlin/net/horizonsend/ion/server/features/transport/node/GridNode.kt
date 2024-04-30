package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

interface GridNode {
	val parentTransportNetwork: ChunkTransportNetwork
	val x: Int
	val y: Int
	val z: Int

	val key get() = toBlockKey(x, y, z)

	val transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode>

	/**
	 * Collects the neighbors of this node
	 **/
	fun collectNeighbors() {

	}

	/**
	 * Notify neighbors of this node's existence
	 **/
	fun notifyNeighbors() {
		// In every adjacent direction
		for (direction in ADJACENT_BLOCK_FACES) {
			val newX = x + direction.modX
			val newY = y + direction.modY
			val newZ = z + direction.modZ

			// All nodes should already be collected
			val possibleNode = parentTransportNetwork.getNode(newX, newY, newZ) ?: continue

			possibleNode.transferableNeighbors.clear()
		}
	}

	/**
	 * Notifies this node of a changed neighbor
	 *
	 * Will handle adding it to the transferable list, if able
	 **/
	fun neighborChanged(offset: BlockFace, replacement: GridNode?) {
		if (replacement != null) {
			if (!isTransferableTo(offset, replacement)) return

			transferableNeighbors[offset] = replacement
		} else {
			transferableNeighbors.remove(offset)
		}
	}

	/**
	 * Whether a power transfer may take place between these two nodes
	 *
	 * Used when initially building relations
	 **/
	fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean

	/**
	 * Returns a possible neighbor of this node
	 **/
	fun getNeighbor(face: BlockFace): GridNode? {
		return transferableNeighbors[face]
	}

	/**
	 * Replace this node with another and update its neighbors
	 **/
	fun replace(new: GridNode?) {
//		for ((offset, neighbor) in transferableNeighbors) {
//			neighbor.neighborChanged(offset.oppositeFace, new)
//
//			parentTransportNetwork.nodes.setOrRemove(key, new)
//		}
	}

	fun processStep(step: Step)
}
