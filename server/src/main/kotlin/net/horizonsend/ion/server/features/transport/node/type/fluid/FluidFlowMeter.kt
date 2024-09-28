package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.FlowMeter
import net.horizonsend.ion.server.features.transport.node.type.general.UnTransferableNode
import net.kyori.adventure.text.Component

class FluidFlowMeter(override val manager: FluidNodeManager) : FlowMeter(), FluidPathfindingNode {
	override val type: NodeType = NodeType.FLUID_FLOW_METER

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?, resource: PipedFluid): ArrayDeque<TransportNode> = cachedTransferable

	override fun formatFlow(): Component {
		return Component.text("bbb")
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is UnTransferableNode
	}

	override fun canTransfer(resource: PipedFluid): Boolean {
		return true
	}
}
