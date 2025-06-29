package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.GraphManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import java.util.UUID

@Suppress("UnstableApiUsage")
class FluidGraph(uuid: UUID, override val manager: GraphManager<*, *>) : TransportNodeGraph<FluidNode>(uuid, manager) {
	override fun getEdge(
		nodeOne: FluidNode,
		nodeTwo: FluidNode,
	): GraphEdge {
		return object : GraphEdge {
			override val nodeOne: GraphNode = nodeOne
			override val nodeTwo: GraphNode = nodeTwo
		}
	}

	val inputs = mutableSetOf<Any>()
	val outputs = mutableSetOf<Any>()

	var contents: FluidStack = FluidStack(FluidTypeKeys.EMPTY.getValue(), 0)

	var cachedVolume: Double? = null

	fun getVolume(): Double {
		val new = networkGraph.nodes().sumOf { it.volume }
		cachedVolume = new
		return new
	}

	override fun onNodeAdded(new: FluidNode) {
		if (new is Input) inputs.add(new)
		if (new is Output) outputs.add(new)

		cachedVolume = null
	}
}
