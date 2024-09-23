package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.NodeType.SPONGE_NODE
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.JunctionNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

/**
 * Represents a sponge [omnidirectional pipe]
 *
 * Since there is no use in keeping the individual steps, all touching sponges are consolidated into a single node with multiple inputs / outputs, weighted evenly
 **/
class SpongeNode(network: PowerNodeManager) : JunctionNode<PowerNodeManager, SpongeNode, SpongeNode>(network), PowerPathfindingNode {
	override val type: NodeType = SPONGE_NODE

	override fun addBack(position: BlockKey) {
		manager.nodeFactory.addJunctionNode<SpongeNode>(position, type, handleRelationships = false)
	}

	override fun getNextNodes(previous: TransportNode): ArrayDeque<TransportNode> = cachedTransferable

	override fun toString(): String = "(SPONGE NODE: ${positions.size} positions, Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes) location = ${toVec3i(positions.random())}"
}
