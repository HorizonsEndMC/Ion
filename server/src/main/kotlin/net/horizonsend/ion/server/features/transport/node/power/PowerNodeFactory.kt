package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode.Companion.matchesSolarPanelStructure
import net.horizonsend.ion.server.miscellaneous.utils.CARDINAL_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.filterValuesIsInstance
import net.horizonsend.ion.server.miscellaneous.utils.mapNotNullTo
import net.horizonsend.ion.server.miscellaneous.utils.popMaxByOrNull
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.data.Directional

object PowerNodeFactory : NodeFactory<ChunkPowerNetwork>() {
	override suspend fun create(network: ChunkPowerNetwork, key: BlockKey, snapshot: BlockSnapshot) {
		if (network.nodes.contains(key)) return

		when {
			// Straight wires
			snapshot.type == Material.END_ROD -> addEndRod(network, snapshot.data as Directional, key)

			// Omnidirectional wires
			snapshot.type == Material.SPONGE -> addSponge(network, key)

			// Extract power from storage
			snapshot.type == Material.CRAFTING_TABLE -> if (matchesSolarPanelStructure(network.world, key)) {
				addSolarPanel(network, key)
			} else {
				addExtractor(network, key)
			}

			// Check for extractor beneath
			snapshot.type == Material.DIAMOND_BLOCK -> {
				val extractorKey = getRelative(key, DOWN, 1)
				network.nodes.remove(extractorKey)
				if (matchesSolarPanelStructure(network.world, extractorKey)) {
					addSolarPanel(network, extractorKey)
				}
			}

			snapshot.type == Material.DAYLIGHT_DETECTOR -> {
				val extractorKey = getRelative(key, DOWN, 2)
				network.nodes.remove(extractorKey)
				if (matchesSolarPanelStructure(network.world, extractorKey)) {
					addSolarPanel(network, extractorKey)
				}
			}

			// Add power to storage
			snapshot.type == Material.NOTE_BLOCK -> addInput(network, key)

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

	fun addSponge(network: ChunkPowerNetwork, position: BlockKey) {
		val neighbors = getNeighborNodes(position, network.nodes).filterValuesIsInstance<SpongeNode, BlockFace, TransportNode>()

		when (neighbors.size) {
			// New sponge node
			0 -> {
				network.nodes[position] = SpongeNode(position)
			}

			// Consolidate into neighbor
			1 -> {
				val neighbor = neighbors.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")
				neighbor.addPosition(network, position)
			}

			// Join multiple neighbors together
			in 2..6 -> {
				// Get the larger
				val spongeNeighbors: MutableMap<BlockFace, SpongeNode> = mutableMapOf()
				neighbors.mapNotNullTo(spongeNeighbors) { (key, value) -> (value as? SpongeNode)?.let { key to value } }

				// Get the largest neighbor
				val largestNeighbor = spongeNeighbors.popMaxByOrNull { it.value.positions.size }?.value ?: throw ConcurrentModificationException("Node removed during processing")

				// Merge all other connected nodes into the largest
				spongeNeighbors.forEach {
					it.value.drainTo(network, largestNeighbor)
				}

				// Add this node
				largestNeighbor.addPosition(network, position)
			}

			else -> throw NotImplementedError()
		}
	}

	suspend fun addEndRod(network: ChunkPowerNetwork, data: Directional, position: Long) {
		val axis = data.facing.axis

		// The neighbors in the direction of the wire's facing, that are also facing that direction
		val neighbors = getNeighborNodes(position, network.nodes, axis.faces.toList()).filterKeys {
			val relative = getRelative(position, it)
			(getBlockSnapshotAsync(network.world, relative, false)?.data as? Directional)?.facing?.axis == axis
		}.filterValuesIsInstance<EndRodNode, BlockFace, TransportNode>()

		when (neighbors.size) {
			// Disconnected
			0 -> {
				network.nodes[position] = EndRodNode(position)
			}

			1 -> {
				val neighbor = neighbors.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")
				neighbor.addPosition(network, position)
			}

			// Should be a max of 2
			2 -> {
				// Get the larger
				val wireNeighbors: MutableMap<BlockFace, EndRodNode> = mutableMapOf()
				neighbors.mapNotNullTo(wireNeighbors) { (key, value) -> (value as? EndRodNode)?.let { key to value } }

				// Get the largest neighbor
				val largestNeighbor = wireNeighbors.popMaxByOrNull { it.value.positions.size }?.value ?: throw ConcurrentModificationException("Node removed during processing")

				// Merge all other connected nodes into the largest
				wireNeighbors.forEach {
					it.value.drainTo(network, largestNeighbor)
				}

				// Add this node
				largestNeighbor.addPosition(network, position)
			}

			else -> throw IllegalArgumentException("Linear node had more than 2 neighbors")
		}
	}

	fun addExtractor(network: ChunkPowerNetwork, position: BlockKey) {
		network.nodes[position] = PowerExtractorNode(position)
	}

	fun addInput(network: ChunkPowerNetwork, position: BlockKey) {
		network.nodes[position] = PowerInputNode(position)
	}

	/**
	 * Provided the key of the extractor, create or combine solar panel nodes
	 **/
	suspend fun addSolarPanel(network: ChunkPowerNetwork, position: BlockKey) {
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
		val neighboringNodes = CARDINAL_BLOCK_FACES.associateWithNotNull { direction ->
			val relativeSide = getRelative(position, direction)

			(-1..3).firstNotNullOfOrNull {
				val neighborKey = getRelative(relativeSide, BlockFace.UP, it)
				val node = network.nodes[neighborKey]
				if (node !is SolarPanelNode) return@firstNotNullOfOrNull null

				// Take only extractor locations
				node.takeIf { node.isIntact(network.world, neighborKey) }
			}
		}.toMutableMap()

		when (neighboringNodes.size) {
			0 -> {
				val node = SolarPanelNode()

				// Add new position
				node.addPosition(network, position, panelPositions)
			}

			1 -> {
				val neighbor = neighboringNodes.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")

				// Add new position to neighbor
				neighbor.addPosition(network, position, panelPositions)
			}

			in 2..4 -> {
				val largestNeighbor = neighboringNodes
					.popMaxByOrNull { it.value.positions.size }
					?.value ?: throw ConcurrentModificationException("Node removed during processing")

				// Merge all other connected nodes into the largest
				neighboringNodes.forEach {
					it.value.drainTo(network, largestNeighbor)
				}

				// Add this node
				largestNeighbor.addPosition(network, position, panelPositions)
			}

			else -> throw IllegalArgumentException()
		}
	}
}
