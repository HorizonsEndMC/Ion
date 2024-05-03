package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.filterValuesIsInstance
import net.horizonsend.ion.server.miscellaneous.utils.mapNotNullTo
import net.horizonsend.ion.server.miscellaneous.utils.popMaxByOrNull
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional

object PowerNodeFactory : NodeFactory<ChunkPowerNetwork>() {
	override suspend fun create(network: ChunkPowerNetwork, key: BlockKey, snapshot: BlockSnapshot): TransportNode? {
		println("Triggering event")

		return when {
			// Extract power from storage
//			block.type == Material.CRAFTING_TABLE -> PowerExtractorNode(this, x, y, z).apply { extractors[toBlockKey(x, y, z)] = this }

			// Add power to storage
//			block.type == Material.NOTE_BLOCK -> PowerInputNode(this, x, y, z)

			// Straight wires
			snapshot.type == Material.END_ROD -> getNodeForPosition(network, snapshot.data as Directional, key)

			// Omnidirectional wires
			snapshot.type == Material.SPONGE -> addSpongeNode(network, key)

			// Merge node behavior
//			block.type == Material.IRON_BLOCK -> MergeNode(this, x, y, z)
//			block.type == Material.REDSTONE_BLOCK -> MergeNode(this, x, y, z)

			// Split power evenly
//			block.customBlock == CustomBlocks.ALUMINUM_BLOCK -> SplitterNode(this, x, y, z)

			// Redstone controlled gate
//			block.type.isRedstoneLamp -> GateNode(this, x, y, z)

			// Power flow meter

			else -> throw NotImplementedError()
		}
	}

	fun addSpongeNode(network: ChunkPowerNetwork, position: BlockKey): SpongeNode {
		val neighbors = getNeighborNodes(position, network.nodes).filterValuesIsInstance<SpongeNode, BlockFace, TransportNode>()

		when (neighbors.size) {
			// New sponge node
			0 -> return SpongeNode(position)
			// Consolidate into neighbor
			1 -> {
				val neighbor = neighbors.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")

				neighbor.positions += position
				return neighbor
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
					it.value.drainTo(largestNeighbor, network.nodes)
				}

				// Add this node
				largestNeighbor.positions.add(position)
				return largestNeighbor
			}

			else -> throw NotImplementedError()
		}
	}

	suspend fun getNodeForPosition(network: ChunkPowerNetwork, data: Directional, position: Long): EndRodNode {
		val axis = data.facing.axis

		// The neighbors in the direction of the wire's facing, that are also facing that direction
		val neighbors = getNeighborNodes(position, network.nodes, axis.faces.toList()).filterKeys {
			val relative = getRelative(position, it)
			(getBlockSnapshotAsync(network.world, relative, false)?.data as? Directional)?.facing?.axis == axis
		}.filterValuesIsInstance<EndRodNode, BlockFace, TransportNode>()

		when (neighbors.size) {
			// Disconnected
			0 -> return EndRodNode(position)

			1 -> {
				val neighbor = neighbors.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")
				neighbor.positions += position

				return neighbor
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
					it.value.drainTo(largestNeighbor, network.nodes)
				}

				// Add this node
				largestNeighbor.positions.add(position)
				return largestNeighbor
			}

			else -> throw IllegalArgumentException("Linear node had more than 2 neighbors")
		}
	}
}
