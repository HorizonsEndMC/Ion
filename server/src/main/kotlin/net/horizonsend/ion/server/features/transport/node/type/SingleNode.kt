package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.power.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative

/**
 * A node that only occupies a single block
 **/
interface SingleNode : TransportNode {
	val position: Long

	override fun handlePlacement(network: ChunkTransportNetwork) {
		network.nodes[position] = this
	}

	override suspend fun buildRelations(network: ChunkTransportNetwork, position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.nodes[offsetKey] ?: continue

			if (this == neighborNode) return

			if (isTransferable(offsetKey, neighborNode)) {
				transferableNeighbors.add(neighborNode)
			}
		}
	}
}
