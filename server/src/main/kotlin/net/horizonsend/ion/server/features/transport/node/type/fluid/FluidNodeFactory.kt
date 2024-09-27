package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_EXTRACTOR_NODE
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_FLOW_METER
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_INPUT
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_INVERTED_DIRECTIONAL_NODE
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_JUNCTION
import net.horizonsend.ion.server.features.transport.node.NodeType.LIGHTNING_ROD
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.util.NodeFactory
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.isChiseledCopper
import net.horizonsend.ion.server.miscellaneous.utils.isCopperBlock
import net.horizonsend.ion.server.miscellaneous.utils.isCopperBulb
import org.bukkit.Material
import org.bukkit.Material.FLETCHING_TABLE
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional

class FluidNodeFactory(network: FluidNodeManager) : NodeFactory<FluidNodeManager>(network) {
	override fun create(key: BlockKey, data: BlockData): Boolean {
		if (network.nodes.contains(key)) return false

		when {
			data.material == Material.LIGHTNING_ROD -> addLinearNode<FluidLinearNode>(key, (data as Directional).facing.axis, LIGHTNING_ROD)
			data.material.isCopperBlock -> addJunctionNode<FluidJunctionNode>(key, FLUID_JUNCTION)

			data.material == Material.CRAFTING_TABLE -> addSimpleSingleNode(key, FLUID_EXTRACTOR_NODE)
			data.material == FLETCHING_TABLE -> addSimpleSingleNode(key, FLUID_INPUT)

			data.material == OBSERVER -> addDirectionalNode(key, (data as Directional).facing, FLUID_FLOW_METER)

			data.material == REDSTONE_BLOCK -> addMergeNode(key, REDSTONE_BLOCK)
			data.material == IRON_BLOCK -> addMergeNode(key, IRON_BLOCK)
			data.material == Material.LAPIS_BLOCK -> addSimpleSingleNode(key, FLUID_INVERTED_DIRECTIONAL_NODE)

			data.material.isChiseledCopper -> println("TODO")

			data.material.isCopperBulb -> println("TODO")

			else -> return false
		}

		return true
	}

	private fun addMergeNode(key: BlockKey, variant: Material) {
		network.nodes[key] = FluidDirectionalNode(network, key, variant).apply {
			onPlace(position)
		}
	}
}
