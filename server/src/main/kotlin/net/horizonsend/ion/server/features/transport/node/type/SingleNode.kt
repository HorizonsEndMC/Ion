package net.horizonsend.ion.server.features.transport.node.type

import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.power.TransportNode

/**
 * A node that only occupies a single block
 **/
interface SingleNode : TransportNode {
	val position: Long

	override fun handlePlacement(network: ChunkTransportNetwork) {
		network.nodes[position] = this
	}
}
