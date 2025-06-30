package net.horizonsend.ion.server.features.transport.manager.graph.fluid

import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.manager.graph.GraphManager
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeGraph
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.Material
import java.util.UUID

@Suppress("UnstableApiUsage")
class FluidGraph(uuid: UUID, override val manager: GraphManager<*, *>) : TransportNodeGraph<FluidNode>(uuid, manager) {
	override val cacheFactory: BlockBasedCacheFactory<FluidNode, TransportNodeGraph<FluidNode>> = BlockBasedCacheFactory.builder<FluidNode, TransportNodeGraph<FluidNode>>()
		.addSimpleNode(Material.BLACK_STAINED_GLASS) { pos, _, holder ->
			object : FluidNode {
				override val volume: Double = 1.0
				override val location: BlockKey = toBlockKey(holder.manager.transportManager.getLocalCoordinate(toVec3i(pos)))

				override fun isIntact() {}

				override fun setGraph(graph: TransportNodeGraph<*>) {}

				override fun getGraph(): TransportNodeGraph<*> { TODO() }
			}
		}
		.build()

	override fun getEdge(
		nodeOne: FluidNode,
		nodeTwo: FluidNode,
	): GraphEdge {
		return object : GraphEdge {
			override val nodeOne: GraphNode = nodeOne
			override val nodeTwo: GraphNode = nodeTwo
		}
	}

	val ports = mutableSetOf<FluidPort>()

	var contents: FluidStack = FluidStack.empty()
	var cachedVolume: Double? = null

	fun getVolume(): Double {
		val new = networkGraph.nodes().sumOf { it.volume }
		cachedVolume = new
		return new
	}

	override fun onNodeAdded(new: FluidNode) {
		if (new is FluidPort) ports.add(new)

		cachedVolume = null
	}

	fun withdrawFluids() {

	}

	fun depositFluids() {

	}

	override fun tick() {
		val volume = cachedVolume ?: getVolume()

		if (contents.amount > volume) {
			contents.amount = volume
		}

		withdrawFluids()
		depositFluids()
	}
}
