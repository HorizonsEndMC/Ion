package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.JunctionNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

class FluidJunctionNode(network: FluidNodeManager) : JunctionNode<FluidNodeManager, FluidJunctionNode, FluidJunctionNode>(network) {
	override val type: NodeType = NodeType.FLUID_JUNCTION
	constructor(network: FluidNodeManager, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override fun addBack(position: BlockKey) {
		manager.nodeFactory.addJunctionNode<FluidJunctionNode>(position, type, handleRelationships = false)
	}

	override fun toString(): String {
		return """
			[Gas Junction Node]
			${positions.size} positions
			Relationships: ${relationHolder.raw().entries.joinToString { (key, relations) -> "[${toVec3i(key)} = (${relations.joinToString { it.other.toString() }}(]" }}
		""".trimIndent()
	}
}