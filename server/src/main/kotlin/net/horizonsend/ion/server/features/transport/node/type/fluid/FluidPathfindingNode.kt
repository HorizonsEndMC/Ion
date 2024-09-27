package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.TransportNode

interface FluidPathfindingNode {
	fun getNextNodes(previous: TransportNode, destination: TransportNode?, resource: PipedFluid): ArrayDeque<TransportNode>
}
