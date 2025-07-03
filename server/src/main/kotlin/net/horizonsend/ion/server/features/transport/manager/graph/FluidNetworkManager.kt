package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.Input
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import org.bukkit.Material
import java.util.UUID

class FluidNetworkManager(manager: TransportHolder) : NetworkManager<FluidNode, TransportNetwork<FluidNode>>(manager) {

	override val cacheFactory: BlockBasedCacheFactory<FluidNode, NetworkManager<FluidNode, TransportNetwork<FluidNode>>> = BlockBasedCacheFactory.builder<FluidNode, NetworkManager<FluidNode, TransportNetwork<FluidNode>>>()
		.addSimpleNode(Material.COPPER_GRATE) { pos, _, holder -> FluidNode.RegularPipe(pos) }
		.addSimpleNode(Material.LIGHTNING_ROD) { pos, _, holder -> FluidNode.SraightPipe(pos) }
		.addSimpleNode(Material.FLETCHING_TABLE) { pos, _, holder -> Input(pos) }
		.build()

	override fun networkProvider(): FluidNetwork {
		return FluidNetwork(UUID.randomUUID(), this)
	}
}
