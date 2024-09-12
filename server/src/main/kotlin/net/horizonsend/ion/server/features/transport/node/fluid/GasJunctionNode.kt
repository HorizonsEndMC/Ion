package net.horizonsend.ion.server.features.transport.node.fluid

import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class GasJunctionNode(network: FluidNodeManager) : JunctionNode<FluidNodeManager, GasJunctionNode, GasJunctionNode>(network) {
	constructor(network: FluidNodeManager, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override suspend fun addBack(position: BlockKey) {
		manager.nodeFactory.addJunction(position, handleRelationships = false)
	}

	override fun toString(): String {
		return """
			[Gas Junction Node]
			${positions.size} positions
			Relationships: ${relationships.joinToString { it.sideTwo.toString() }}
		""".trimIndent()
	}
}
