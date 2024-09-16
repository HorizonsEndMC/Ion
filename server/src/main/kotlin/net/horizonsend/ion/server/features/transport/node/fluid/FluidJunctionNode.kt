package net.horizonsend.ion.server.features.transport.node.fluid

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.features.transport.node.manager.node.FluidNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

class FluidJunctionNode(network: FluidNodeManager) : JunctionNode<FluidNodeManager, FluidJunctionNode, FluidJunctionNode>(network) {
	override val type: NodeType = NodeType.FLUID_JUNCTION
	constructor(network: FluidNodeManager, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override fun addBack(position: BlockKey) {
		manager.nodeFactory.addJunction(position, handleRelationships = false)
	}

	override fun toString(): String {
		return """
			[Gas Junction Node]
			${positions.size} positions
			Relationships: ${relationships.values.joinToString { it.other.toString() }}
		""".trimIndent()
	}
}
