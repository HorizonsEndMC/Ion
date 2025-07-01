package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraph
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.Input
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import org.bukkit.Material
import java.util.UUID

class FluidGraphManager(manager: TransportHolder) : GraphManager<FluidNode, FluidGraph>(manager) {
	override val cacheFactory: BlockBasedCacheFactory<FluidNode, GraphManager<FluidNode, FluidGraph>> = BlockBasedCacheFactory.builder<FluidNode, GraphManager<FluidNode, FluidGraph>>()
		.addSimpleNode(Material.COPPER_GRATE) { pos, _, holder -> FluidNode.RegularPipe(pos) }
		.addSimpleNode(Material.FLETCHING_TABLE) { pos, _, holder -> Input(pos) }
		.build()

	override fun createGraph(): FluidGraph {
		return FluidGraph(UUID.randomUUID(), this)
	}
}
