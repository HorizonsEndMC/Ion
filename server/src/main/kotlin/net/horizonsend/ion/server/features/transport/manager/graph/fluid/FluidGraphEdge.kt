package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode

class FluidGraphEdge(
	override val nodeOne: TransportNode,
	override val nodeTwo: TransportNode
) : GraphEdge {
	/** returns the net flow between the two nodes. If positive, it is towards node two, if negative, towards node one. */
	var netFlow: Double = 0.0
}
