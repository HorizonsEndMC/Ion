package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

/**
 * Represents a sponge [omnidirectional pipe]
 *
 * Since there is no use in keeping the individual steps, all touching sponges are consolidated into a single node with multiple inputs / outputs, weighted evenly
 **/
class SpongeNode(network: PowerNodeManager) : JunctionNode<PowerNodeManager, SpongeNode, SpongeNode>(network) {
	override val type: NodeType = NodeType.SPONGE_NODE

	constructor(network: PowerNodeManager, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override fun addBack(position: BlockKey) {
		manager.nodeFactory.addSponge(position, handleRelationships = false)
	}

	override fun toString(): String = "(SPONGE NODE: ${positions.size} positions, Transferable to: ${getTransferableNodes().joinToString { it.first.javaClass.simpleName }} nodes) location = ${toVec3i(positions.random())}"
}
