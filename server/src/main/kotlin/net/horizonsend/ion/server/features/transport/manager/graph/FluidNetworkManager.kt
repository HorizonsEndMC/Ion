package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.custom.blocks.pipe.FluidPipeBlock
import net.horizonsend.ion.server.features.custom.blocks.pipe.ReinforcedFluidPipeBlock
import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork.Companion.key
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidPort
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNode.FluidValve
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.MultipleFacing
import java.util.UUID

class FluidNetworkManager(manager: TransportHolder) : NetworkManager<FluidNode, TransportNetwork<FluidNode>>(manager) {
	override val namespacedKey: NamespacedKey = key

	override val cacheFactory = cache
	override fun networkProvider(): FluidNetwork {
		return FluidNetwork(UUID.randomUUID(), this)
	}

	private companion object {
		@JvmStatic
		val cache: BlockBasedCacheFactory<FluidNode, NetworkManager<FluidNode, TransportNetwork<FluidNode>>> = BlockBasedCacheFactory.builder<FluidNode, NetworkManager<FluidNode, TransportNetwork<FluidNode>>>()
			.addDataHandler<MultipleFacing>(CustomBlockKeys.FLUID_PIPE_JUNCTION, Material.CHORUS_PLANT) { _, pos, holder ->
				FluidNode.RegularJunctionPipe(pos)
			}
			.addDataHandler<MultipleFacing>(CustomBlockKeys.FLUID_PIPE, Material.CHORUS_PLANT) { data, pos, holder ->
				val axis = (data.customBlock as FluidPipeBlock).getFace(data)
				FluidNode.RegularLinearPipe(pos, axis)
			}
			.addDataHandler<MultipleFacing>(CustomBlockKeys.REINFORCED_FLUID_PIPE_JUNCTION, Material.CHORUS_PLANT) { _, pos, holder ->
				FluidNode.ReinforcedJunctionPipe(pos)
			}
			.addDataHandler<MultipleFacing>(CustomBlockKeys.REINFORCED_FLUID_PIPE, Material.CHORUS_PLANT) { data, pos, holder ->
				val axis = (data.customBlock as ReinforcedFluidPipeBlock).getFace(data)
				FluidNode.ReinforcedLinearPipe(pos, axis)
			}
			.addDataHandler<MultipleFacing>(CustomBlockKeys.FLUID_INPUT, Material.BROWN_MUSHROOM_BLOCK) { _, pos, holder -> FluidPort(pos) }
			.addDataHandler<MultipleFacing>(CustomBlockKeys.FLUID_VALVE, Material.BROWN_MUSHROOM_BLOCK) { _, pos, holder -> FluidValve(pos) }
			.build()
	}
}
