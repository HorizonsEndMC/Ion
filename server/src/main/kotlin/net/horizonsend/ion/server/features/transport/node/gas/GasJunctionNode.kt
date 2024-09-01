package net.horizonsend.ion.server.features.transport.node.gas

import net.horizonsend.ion.server.features.transport.network.FluidNetwork
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class GasJunctionNode(network: FluidNetwork) : JunctionNode<FluidNetwork, GasJunctionNode, GasJunctionNode>(network) {
	constructor(network: FluidNetwork, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override suspend fun addBack(position: BlockKey) {
		network.nodeFactory.addJunction(position, handleRelationships = false)
	}

	override fun toString(): String {
		return """
			[Gas Junction Node]
			${positions.size} positions
			Relationships: ${relationships.joinToString { it.sideTwo.toString() }}
		""".trimIndent()
	}
}
