package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

interface FluidNode : GraphNode {
	val volume: Double

	class RegularPipe(override val location: BlockKey) : FluidNode {
		override val volume: Double = 100.0

		override fun isIntact(): Boolean {
			return true
		}

		override fun setGraph(graph: TransportNodeGraph<*>) {
			TODO("Not yet implemented")
		}

		override fun getGraph(): TransportNodeGraph<*> {
			TODO("Not yet implemented")
		}
	}

	class Input(override val location: BlockKey) : FluidNode {
		override val volume: Double = 100.0

		override fun isIntact(): Boolean {
			return true
		}

		override fun setGraph(graph: TransportNodeGraph<*>) {
			TODO("Not yet implemented")
		}

		override fun getGraph(): TransportNodeGraph<*> {
			TODO("Not yet implemented")
		}
	}
}
