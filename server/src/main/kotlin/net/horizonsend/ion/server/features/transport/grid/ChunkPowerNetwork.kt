package net.horizonsend.ion.server.features.transport.grid

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.power.EndRodNode
import net.horizonsend.ion.server.features.transport.node.power.SpongeNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.IntervalExecutor
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.faces
import net.horizonsend.ion.server.miscellaneous.utils.mapNotNullTo
import net.horizonsend.ion.server.miscellaneous.utils.popMaxByOrNull
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import java.util.concurrent.ConcurrentHashMap

class ChunkPowerNetwork(manager: ChunkTransportManager) : ChunkTransportNetwork(manager) {
	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()
//	val extractors = ConcurrentHashMap<Long, PowerExtractorNode>()

	override val namespacedKey: NamespacedKey = NamespacedKeys.POWER_TRANSPORT

	override fun setup() {
		collectPowerMultiblockEntities()
	}

	override suspend fun createNodeFromBlock(block: BlockSnapshot) {
		println("Triggering event")
		val x = block.x
		val y = block.y
		val z = block.z

		val key = toBlockKey(x, y, z)

		when {
			// Extract power from storage
//			block.type == Material.CRAFTING_TABLE -> PowerExtractorNode(this, x, y, z).apply { extractors[toBlockKey(x, y, z)] = this }

			// Add power to storage
//			block.type == Material.NOTE_BLOCK -> PowerInputNode(this, x, y, z)

			// Straight wires
			block.type == Material.END_ROD -> addEndRodNode(block.data as Directional, key)

			// Omnidirectional wires
			block.type == Material.SPONGE -> addSpongeNode(key)

			// Merge node behavior
//			block.type == Material.IRON_BLOCK -> MergeNode(this, x, y, z)
//			block.type == Material.REDSTONE_BLOCK -> MergeNode(this, x, y, z)

			// Split power evenly
//			block.customBlock == CustomBlocks.ALUMINUM_BLOCK -> SplitterNode(this, x, y, z)

			// Redstone controlled gate
//			block.type.isRedstoneLamp -> GateNode(this, x, y, z)

			// Power flow meter
//			block.type == Material.OBSERVER -> PowerFlowMeter(this, x, y, z)
		}
	}

	override fun processBlockRemoval(key: Long) { manager.scope.launch {
		val previousNode = nodes[key] ?: return@launch

		previousNode.handleRemoval(this@ChunkPowerNetwork, key)
	}}

	override fun processBlockAddition(key: Long, new: BlockSnapshot) { manager.scope.launch {
		createNodeFromBlock(new)
	}}

	fun removeNode(key: Long) {
		nodes.remove(key)
	}

	val tickExecutor = IntervalExecutor(20) {

	}

	override fun tick() {
//		tickExecutor()
	}

	private fun collectPowerMultiblockEntities() {
		manager.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}

	fun addSpongeNode(position: Long) {
		val neighbors = getNeighborNodes(position, nodes).filter { it.value is SpongeNode }

		when (neighbors.size) {
			// New sponge node
			0 -> nodes[position] = SpongeNode(position)
			// Consolidate into neighbor
			1 -> {
				val neighbor = neighbors.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")
				if (neighbor !is SpongeNode) return

				neighbor.positions += position
				nodes[position] = neighbor
			}

			// Join multiple neighbors together
			in 2..6 -> {
				// Get the larger
				val spongeNeighbors: MutableMap<BlockFace, SpongeNode> = mutableMapOf()
				neighbors.mapNotNullTo(spongeNeighbors) { (key, value) -> (value as? SpongeNode)?.let { key to value } }

				// Get the largest neighbor
				val largestNeighbor = spongeNeighbors.popMaxByOrNull { it.value.positions.size }?.value ?: return

				// Merge all other connected nodes into the largest
				spongeNeighbors.forEach {
					it.value.drainTo(largestNeighbor, this.nodes)
				}

				// Add this node
				largestNeighbor.positions.add(position)
				nodes[position] = largestNeighbor
			}
		}
	}

	suspend fun addEndRodNode(data: Directional, position: Long) {
		val axis = data.facing.axis

		// The neighbors in the direction of the wire's facing, that are also facing that direction
		val neighbors = getNeighborNodes(position, nodes, axis.faces.toList()).filterKeys {
			val relative = getRelative(position, it)
			(getBlockSnapshotAsync(world, relative, false)?.data as? Directional)?.facing?.axis == axis
		}.filter { it.value is EndRodNode }

		when (neighbors.size) {
			// Disconnected
			0 -> nodes[position] = EndRodNode(position)

			1 -> {
				val neighbor = neighbors.values.firstOrNull() ?: throw ConcurrentModificationException("Node removed during processing")
				if (neighbor !is EndRodNode) return

				neighbor.positions += position
				nodes[position] = neighbor
			}

			// Should be a max of 2
			2 -> {
				// Get the larger
				val wireNeighbors: MutableMap<BlockFace, EndRodNode> = mutableMapOf()
				neighbors.mapNotNullTo(wireNeighbors) { (key, value) -> (value as? EndRodNode)?.let { key to value } }

				// Get the largest neighbor
				val largestNeighbor = wireNeighbors.popMaxByOrNull { it.value.positions.size }?.value ?: return

				// Merge all other connected nodes into the largest
				wireNeighbors.forEach {
					it.value.drainTo(largestNeighbor, this.nodes)
				}

				// Add this node
				largestNeighbor.positions.add(position)
				nodes[position] = largestNeighbor
			}

			else -> throw IllegalArgumentException("Linear node had more than 2 neighbors")
		}
	}
}
