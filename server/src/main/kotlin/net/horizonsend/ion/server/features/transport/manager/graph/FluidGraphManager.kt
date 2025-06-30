package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraph
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import java.util.UUID

class FluidGraphManager(manager: TransportManager<*>) : GraphManager<FluidNode, FluidGraph>(manager) {
	override fun createGraph(): FluidGraph {
		return FluidGraph(UUID.randomUUID(), this)
	}
}
