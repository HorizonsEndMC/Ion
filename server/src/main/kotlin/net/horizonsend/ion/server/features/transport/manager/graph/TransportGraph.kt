package net.horizonsend.ion.server.features.transport.manager.graph

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import java.util.UUID

abstract class TransportGraph(val uuid: UUID, val manager: GraphManager) {
	val networkGraph: MutableNetwork<GraphNode, GraphEdge> = NetworkBuilder.directed()
		.allowsParallelEdges(true)
		.allowsSelfLoops(false)
		.build()
}
