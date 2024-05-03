package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.power.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
interface MultiNode : TransportNode {
	/**
	 * The positions occupied by the node
	 **/
	val positions: MutableSet<BlockKey>

	suspend fun rebuildNode(network: ChunkTransportNetwork, position: BlockKey)

	override suspend fun handleRemoval(network: ChunkTransportNetwork, position: BlockKey) {
		network as ChunkPowerNetwork

		network.nodes.remove(position)
		positions.remove(position)

		// Remove all
		positions.forEach {
			network.nodes.remove(it)
		}

		rebuildNode(network, position)
	}

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	fun drainTo(new: MultiNode, nodes: MutableMap<BlockKey, TransportNode>) {
		new.positions.addAll(positions)
		new.transferableNeighbors.addAll(transferableNeighbors)

		for (position in positions) {
			nodes[position] = new
		}
	}

	override fun handlePlacement(network: ChunkTransportNetwork) {
		for (key in positions) {
			network.nodes[key] = this
		}
	}
}
