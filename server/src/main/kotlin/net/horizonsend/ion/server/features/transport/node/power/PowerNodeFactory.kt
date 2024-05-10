package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.handleMerges
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode.Companion.matchesSolarPanelStructure
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.data.Directional

class PowerNodeFactory(network: ChunkPowerNetwork) : NodeFactory<ChunkPowerNetwork>(network) {
	override suspend fun create(key: BlockKey, snapshot: BlockSnapshot) {
		if (network.nodes.contains(key)) return

		when {
			// Straight wires
			snapshot.type == Material.END_ROD -> addEndRod(snapshot.data as Directional, key)

			// Omnidirectional wires
			snapshot.type == Material.SPONGE -> addSponge(key)

			// Extract power from storage
			snapshot.type == Material.CRAFTING_TABLE -> if (matchesSolarPanelStructure(network.world, key)) {
				addSolarPanel(key)
			} else {
				addExtractor(key)
			}

			// Check for extractor beneath
			snapshot.type == Material.DIAMOND_BLOCK -> {
				val extractorKey = getRelative(key, DOWN, 1)
				network.nodes.remove(extractorKey)
				if (matchesSolarPanelStructure(network.world, extractorKey)) {
					addSolarPanel(extractorKey)
				}
			}

			snapshot.type == Material.DAYLIGHT_DETECTOR -> {
				val extractorKey = getRelative(key, DOWN, 2)
				network.nodes.remove(extractorKey)
				if (matchesSolarPanelStructure(network.world, extractorKey)) {
					addSolarPanel(extractorKey)
				}
			}

			// Add power to storage
			snapshot.type == Material.NOTE_BLOCK -> addInput(key)
			snapshot.type == Material.OBSERVER -> addFlowMeter(key)

			// Merge node behavior
//			block.type == Material.IRON_BLOCK -> MergeNode(this, x, y, z)
//			block.type == Material.REDSTONE_BLOCK -> MergeNode(this, x, y, z)

			// Split power evenly
//			block.customBlock == CustomBlocks.ALUMINUM_BLOCK -> SplitterNode(this, x, y, z)

			// Redstone controlled gate
//			block.type.isRedstoneLamp -> GateNode(this, x, y, z)

			// Power flow meter

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
		val neighbors = getNeighborNodes(position, network.nodes, axis.faces.toList()).filterKeys {
			val relative = getRelative(position, it)
			(getBlockSnapshotAsync(network.world, relative, false)?.data as? Directional)?.facing?.axis == axis
		}.values.filterIsInstanceTo<EndRodNode, MutableList<EndRodNode>>(mutableListOf())

		val finalNode = when (neighbors.size) {
			// Disconnected
			0 ->  EndRodNode(network, position).apply { loadIntoNetwork() }

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

	suspend fun addFlowMeter(position: BlockKey) {
		network.nodes[position] = PowerFlowMeter(network, position).apply {
			onPlace(position)
		}
	}

	/**
	 * Provided the key of the extractor, create or combine solar panel nodes
	 **/
	suspend fun addSolarPanel(position: BlockKey, handleRelationships: Boolean = true) {
		// The diamond and daylight detector
		val panelPositions = (1..2).map { getRelative(position, BlockFace.UP, it) }

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
				network.solarPanels += this
			}.addPosition(position, panelPositions)

			1 -> neighboringNodes.firstOrNull()?.addPosition(position, panelPositions) ?: throw ConcurrentModificationException("Node removed during processing")

			in 2..4 -> handleMerges(neighboringNodes).addPosition(position, panelPositions)

			else -> throw IllegalArgumentException()
		}

		if (handleRelationships) node.rebuildRelations()
	}
}
