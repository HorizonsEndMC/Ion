package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.gridenergy.GridEnergyNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.axis
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.Directional
import org.bukkit.block.data.MultipleFacing
import java.util.UUID

class GridEnergyGraphManager(manager: TransportHolder) : NetworkManager<GridEnergyNode, TransportNetwork<GridEnergyNode>>(manager) {
	override val namespacedKey: NamespacedKey = key

	override val cacheFactory = cache
	override fun networkProvider(): GridEnergyNetwork {
		return GridEnergyNetwork(UUID.randomUUID(), this)
	}

	private companion object {
		private val key = NamespacedKeys.key("grid_energy_network")

		@JvmStatic
		val cache: BlockBasedCacheFactory<GridEnergyNode, NetworkManager<GridEnergyNode, TransportNetwork<GridEnergyNode>>> = BlockBasedCacheFactory.builder<GridEnergyNode, NetworkManager<GridEnergyNode, TransportNetwork<GridEnergyNode>>>()
			.addDataHandler<MultipleFacing>(CustomBlockKeys.GRID_ENERGY_PORT, Material.BROWN_MUSHROOM_BLOCK) { _, pos, holder -> GridEnergyNode.GridEnergyPortNode(pos) }
			.addSimpleNode(Material.SPONGE) { pos, _, _ -> GridEnergyNode.GridEnergyJunctionNode(pos) }
			.addDataHandler<Directional>(Material.END_ROD) { data, pos, _ -> GridEnergyNode.GridEnergyLinearNode(pos, data.facing.axis) }
			.build()
	}
}
