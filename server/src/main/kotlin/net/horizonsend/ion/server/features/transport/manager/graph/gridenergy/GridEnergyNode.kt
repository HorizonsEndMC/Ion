package net.horizonsend.ion.server.features.transport.manager.graph.gridenergy

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNetwork
import net.horizonsend.ion.server.features.transport.manager.graph.TransportNodeType
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import org.bukkit.block.BlockFace

abstract class GridEnergyNode(location: BlockKey, type: TransportNodeType<*>) : TransportNode(location, type) {
	private lateinit var graph: GridEnergyNetwork

	override fun getNetwork(): TransportNetwork<*> = graph
	override fun setNetworkOwner(graph: TransportNetwork<*>) {
		this.graph = graph as GridEnergyNetwork
	}

	class GridEnergyPort(location: BlockKey) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_PORT.getValue()) {
		override fun isIntact(): Boolean? {
			return getBlock()?.blockData?.customBlock?.key == CustomBlockKeys.GRID_ENERGY_PORT
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}

	class GridEnergyJunction(location: BlockKey) : GridEnergyNode(location, TransportNetworkNodeTypeKeys.GRID_ENERGY_JUNCTION.getValue()) {
		override fun isIntact(): Boolean? {
			return getBlock()?.type == Material.SPONGE
		}

		override fun getPipableDirections(): Set<BlockFace> = ADJACENT_BLOCK_FACES
	}
}
