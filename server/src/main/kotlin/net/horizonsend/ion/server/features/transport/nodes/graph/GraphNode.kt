package net.horizonsend.ion.server.features.transport.nodes.graph

import net.horizonsend.ion.server.features.transport.manager.graph.TransportGraph
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

interface GraphNode : Node {
	val location: BlockKey

	fun isIntact()

	fun setGraph(graph: TransportGraph)
	fun getGraph(): TransportGraph
}
