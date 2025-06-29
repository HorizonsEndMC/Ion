package net.horizonsend.ion.server.features.transport.nodes.graph

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i

interface GraphNode {
	val location: BlockKey

	fun getCenter() = toVec3i(location).toCenterVector()

	fun isIntact()

	fun setGraph(graph: TransportNodeGraph<*>)
	fun getGraph(): TransportNodeGraph<*>
}
