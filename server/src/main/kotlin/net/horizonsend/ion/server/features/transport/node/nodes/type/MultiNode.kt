package net.horizonsend.ion.server.features.transport.node.nodes.type

import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.nodes.TransportNode

/**
 * A transport node that may cover many blocks to avoid making unnecessary steps
 **/
interface MultiNode : TransportNode {
	/**
	 * The positions occupied by the node
	 **/
	val positions: MutableSet<Long>

	override fun handleRemoval(network: ChunkTransportNetwork, position: Long) {
		network as ChunkPowerNetwork

		network.nodes.remove(position)
		positions.remove(position)

		// Remove all
		positions.forEach {
			network.nodes.remove(it)
		}

		// Create new nodes, automatically merging together
		positions.forEach {
			network.addSpongeNode(it)
		}
	}

	/**
	 * Drain all the positions and connections to the provided node
	 **/
	fun drainTo(new: MultiNode, nodes: MutableMap<Long, TransportNode>) {
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
