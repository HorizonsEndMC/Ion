package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_EXTRACTOR_NODE
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode

class FluidExtractorNode(override val manager: FluidNodeManager) : SingleNode(), FluidPathfindingNode {
	override val type: NodeType = FLUID_EXTRACTOR_NODE

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is FluidExtractorNode
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int = 0
	override fun getNextNodes(previous: TransportNode, destination: TransportNode?, resource: PipedFluid): ArrayDeque<TransportNode> = cachedTransferable
}
