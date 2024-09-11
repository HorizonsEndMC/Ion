package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.handleMerges
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode.Companion.matchesSolarPanelStructure
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional

class PowerNodeFactory(network: PowerNodeManager) : NodeFactory<PowerNodeManager>(network) {
	override suspend fun create(key: BlockKey, data: BlockData) {
		if (network.nodes.contains(key)) return

		when {
			// Straight wires
			data.material == Material.END_ROD -> addEndRod(data as Directional, key)

			// Omnidirectional wires
			data.material == Material.SPONGE -> addSponge(key)

			// Extract power from storage
			data.material == Material.CRAFTING_TABLE -> {
				if (matchesSolarPanelStructure(network.world, key)) {
					addSolarPanel(key)
				} else {
					addExtractor(key)
				}
			}

			// Check for extractor beneath
			data.material == Material.DIAMOND_BLOCK -> {
				val extractorKey = getRelative(key, DOWN, 1)

				if (matchesSolarPanelStructure(network.world, extractorKey)) {
					network.nodes.remove(extractorKey)
					addSolarPanel(extractorKey)
				}
			}

			data.material == Material.DAYLIGHT_DETECTOR -> {
				val extractorKey = getRelative(key, DOWN, 2)

				if (matchesSolarPanelStructure(network.world, extractorKey)) {
					network.nodes.remove(extractorKey)
					addSolarPanel(extractorKey)
				}
			}

			// Add power to storage
			data.material == Material.NOTE_BLOCK -> addInput(key)

			// Power flow meter
			data.material == Material.OBSERVER -> addFlowMeter(data as Directional, key)

			// Merge node behavior
			data.material == Material.IRON_BLOCK -> addMergeNode(key, Material.IRON_BLOCK)
			data.material == Material.REDSTONE_BLOCK -> addMergeNode(key, Material.REDSTONE_BLOCK)

			// Split power evenly
			CustomBlocks.getByBlockData(data) == CustomBlocks.ALUMINUM_BLOCK -> addEqualSplitterNode(key)

			// Redstone controlled gate
//			block.type.isRedstoneLamp -> GateNode(this, x, y, z)

			else -> return
		}
	}

	suspend fun addSponge(position: BlockKey, handleRelationships: Boolean = true) {
		val neighbors = getNeighborNodes(position, network.nodes).values.filterIsInstanceTo<SpongeNode, MutableList<SpongeNode>>(mutableListOf())

		val finalNode = when (neighbors.size) {
			// New sponge node
			0 -> SpongeNode(network, position).apply { loadIntoNetwork() }

			// Consolidate into neighbor
			1 ->  neighbors.firstOrNull()?.addPosition(position) ?: throw ConcurrentModificationException("Node removed during processing")

			// Join multiple neighbors together
			in 2..6 -> handleMerges(neighbors).addPosition(position)

			else -> throw NotImplementedError()
		}

		if (handleRelationships) finalNode.rebuildRelations()
	}

	suspend fun addEndRod(data: Directional, position: Long, handleRelationships: Boolean = true) {
		val axis = data.facing.axis

		// The neighbors in the direction of the wire's facing, that are also facing that direction
		val neighbors = getNeighborNodes(position, network.nodes, axis.faces.toList())
			.values
			.filterIsInstance<EndRodNode>()
			.filterTo(mutableListOf()) { it.axis == axis }

		val finalNode = when (neighbors.size) {
			// Disconnected
			0 ->  EndRodNode(network, position, data.facing.axis).apply { loadIntoNetwork() }

			// Consolidate into neighbor
			1 -> neighbors.firstOrNull()?.addPosition(position) ?: throw ConcurrentModificationException("Node removed during processing")

			// Should be a max of 2
			2 -> handleMerges(neighbors).addPosition(position)

			else -> throw IllegalArgumentException("Linear node had more than 2 neighbors")
		}

		if (handleRelationships) finalNode.rebuildRelations()
	}

	suspend fun addExtractor(position: BlockKey) {
		network.nodes[position] = PowerExtractorNode(network, position).apply {
			onPlace(position)
		}
	}

	suspend fun addInput(position: BlockKey) {
		network.nodes[position] = PowerInputNode(network, position).apply {
			onPlace(position)
		}
	}

	suspend fun addFlowMeter(data: Directional, position: BlockKey) {
		network.nodes[position] = PowerFlowMeter(network, position, data.facing).apply {
			onPlace(position)
		}
	}

	/**
	 * Provided the key of the extractor, create or combine solar panel nodes
	 **/
	suspend fun addSolarPanel(position: BlockKey, handleRelationships: Boolean = true) {
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
				val neighborKey = getRelative(relativeSide, BlockFace.UP, it)
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

	suspend fun addMergeNode(key: BlockKey, variant: Material) {
		network.nodes[key] = PowerDirectionalNode(network, key, variant).apply {
			onPlace(position)
		}
	}

	suspend fun addInvertedMergeNode(key: BlockKey) {
		network.nodes[key] = InvertedDirectionalNode(network, key).apply {
			onPlace(position)
		}
	}

	suspend fun addEqualSplitterNode(position: BlockKey) {
		network.nodes[position] = PowerEqualSplitterNode(network, position).apply {
			onPlace(position)
		}
	}
}
