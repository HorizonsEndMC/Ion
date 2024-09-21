package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.NodeType.POWER_EXTRACTOR_NODE
import net.horizonsend.ion.server.features.transport.node.NodeType.POWER_INPUT_NODE
import net.horizonsend.ion.server.features.transport.node.NodeType.POWER_INVERSE_DIRECTIONAL_NODE
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.power.SolarPanelNode.Companion.matchesSolarPanelStructure
import net.horizonsend.ion.server.features.transport.node.util.NodeFactory
import net.horizonsend.ion.server.features.transport.node.util.handleMerges
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.Material
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional

class PowerNodeFactory(network: PowerNodeManager) : NodeFactory<PowerNodeManager>(network) {
	override fun create(key: BlockKey, data: BlockData): Boolean {
		if (network.nodes.contains(key)) return false

		when (data.material) {
			Material.END_ROD -> addLinearNode<EndRodNode>(key, (data as Directional).facing.axis, NodeType.END_ROD_NODE)
			Material.SPONGE -> addJunctionNode<SpongeNode>(key, NodeType.SPONGE_NODE)

			// If a solar panel is not created, add an extractor
			Material.CRAFTING_TABLE -> if (!checkSolarPanels(key, 1)) addSimpleSingleNode(key, POWER_EXTRACTOR_NODE)
			Material.DIAMOND_BLOCK -> checkSolarPanels(key, 1)
			Material.DAYLIGHT_DETECTOR -> checkSolarPanels(key, 2)

			Material.NOTE_BLOCK -> addSimpleSingleNode(key, POWER_INPUT_NODE)
			Material.OBSERVER -> addFlowMeter(data as Directional, key)

			Material.IRON_BLOCK -> addMergeNode(key, Material.IRON_BLOCK)
			Material.REDSTONE_BLOCK -> addMergeNode(key, Material.REDSTONE_BLOCK)
			Material.LAPIS_BLOCK -> addSimpleSingleNode(key, POWER_INVERSE_DIRECTIONAL_NODE)

			// Redstone controlled gate
			//			block.type.isRedstoneLamp -> GateNode(this, x, y, z)
			else -> return false
		}

		return true
	}

	private fun addFlowMeter(data: Directional, position: BlockKey) {
		network.nodes[position] = PowerFlowMeter(network, position, data.facing).apply {
			onPlace(position)
		}
	}

	/**
	 * Provided the key of the extractor, create or combine solar panel nodes
	 **/
	fun addSolarPanel(position: BlockKey, handleRelationships: Boolean = true) {
		// The diamond and daylight detector
		val diamondPosition = getRelative(position, UP, 1)
		val detectorPosition = getRelative(position, UP, 2)

		// Get the nodes that might be touching the solar panel
		//
		// 2d cross-section for demonstration: C is the origin crafting table, X are positions checked
		//
		// X   X
		// X   X
		// X C X
		// X   X

		// If another solar panel is found at any of those positions, handle merges
		val neighboringNodes = CARDINAL_BLOCK_FACES.mapNotNullTo(mutableListOf()) { direction ->
			val relativeSide = getRelative(position, direction)

			(-1..3).firstNotNullOfOrNull {
				val neighborKey = getRelative(relativeSide, UP, it)
				val node = network.nodes[neighborKey]
				if (node !is SolarPanelNode) return@firstNotNullOfOrNull null

				// Take only extractor locations
				node.takeIf { node.isIntact(network.world, neighborKey) }
			}
		}

		val node = when (neighboringNodes.size) {
			0 ->  SolarPanelNode(network).apply {
				manager.solarPanels += this
			}.addPosition(position, diamondPosition, detectorPosition)

			1 -> neighboringNodes.firstOrNull()?.addPosition(position, diamondPosition, detectorPosition) ?: throw ConcurrentModificationException("Node removed during processing")

			in 2..4 -> handleMerges(neighboringNodes).addPosition(position, diamondPosition, detectorPosition)

			else -> throw IllegalArgumentException()
		}

		if (handleRelationships) node.rebuildRelations()
	}

	private fun addMergeNode(key: BlockKey, variant: Material) {
		network.nodes[key] = PowerDirectionalNode(network, key, variant).apply {
			onPlace(position)
		}
	}

	private fun checkSolarPanels(position: BlockKey, extractorDistance: Int): Boolean {
		val extractorKey = getRelative(position, DOWN, extractorDistance)

		val check = matchesSolarPanelStructure(network.world, extractorKey)

		if (check) {
			network.nodes.remove(extractorKey)
			addSolarPanel(extractorKey)
		}

		return check
	}
}
