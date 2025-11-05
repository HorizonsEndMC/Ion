package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class FlowNode(location: BlockKey, type: TransportNodeType<*>) : TransportNode(location, type) {
	abstract val flowCapacity: Double
}
