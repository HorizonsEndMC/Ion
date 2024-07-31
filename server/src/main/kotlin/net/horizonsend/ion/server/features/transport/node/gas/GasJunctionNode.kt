package net.horizonsend.ion.server.features.transport.node.gas

import net.horizonsend.ion.server.features.transport.network.GasNetwork
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class GasJunctionNode(network: GasNetwork) : JunctionNode<GasNetwork, GasJunctionNode, GasJunctionNode>(network) {
	constructor(network: GasNetwork, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override suspend fun addBack(position: BlockKey) {
		network.nodeFactory.addJunction(position, handleRelationships = false)
	}
}
