package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode

interface FluidNode : GraphNode {
	val volume: Double
}
