package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.DirectionalNode

class FluidInvertedDirectionalNode(override val manager: FluidNodeManager) : DirectionalNode(), FluidPathfindingNode {
	override val type: NodeType = NodeType.FLUID_INVERTED_DIRECTIONAL_NODE

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is FluidLinearNode) return false
		return node !is FluidExtractorNode
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?, resource: PipedFluid): ArrayDeque<TransportNode> {
		if (destination != null && relationHolder.hasRelationAtWith(position, destination)) return ArrayDeque(listOf(destination))

		// Since this is a single node, and the previous node must be transferable to this, it can't be a sponge.
		// So there will likely only be a single relation to this
		val direction = previous.getRelationshipWith(this).values
		if (direction.isEmpty()) return cachedTransferable // just in case
		val face = direction.first().offset

		getForwardTransferable(face)?.let { return ArrayDeque(listOf(it)) }

		return cachedTransferable
	}
}
