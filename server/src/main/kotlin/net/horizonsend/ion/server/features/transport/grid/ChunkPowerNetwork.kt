package net.horizonsend.ion.server.features.transport.grid

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.general.GateNode
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.nodes.SpongeNode
import net.horizonsend.ion.server.features.transport.node.power.MergeNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.SplitterNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.IntervalExecutor
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.isRedstoneLamp
import net.horizonsend.ion.server.miscellaneous.utils.mapNotNullTo
import net.horizonsend.ion.server.miscellaneous.utils.popMaxByOrNull
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import java.util.concurrent.ConcurrentHashMap

class ChunkPowerNetwork(manager: ChunkTransportManager) : TransportNetwork(manager) {
	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()
	val extractors = ConcurrentHashMap<Long, PowerExtractorNode>()

	override val namespacedKey: NamespacedKey = NamespacedKeys.POWER_TRANSPORT

	override fun setup() {
		collectPowerMultiblockEntities()
	}

	override fun createNodeFromBlock(block: BlockSnapshot) {
		val x = block.x
		val y = block.y
		val z = block.z

		val key = toBlockKey(x, y, z)

		when {
			// Extract power from storage
			block.type == Material.CRAFTING_TABLE -> PowerExtractorNode(this, x, y, z).apply { extractors[toBlockKey(x, y, z)] = this }

			// Add power to storage
			block.type == Material.NOTE_BLOCK -> PowerInputNode(this, x, y, z)

			// Straight wires
			block.type == Material.END_ROD -> LinearNode(this, x, y, z, block.data as Directional)

			// Omnidirectional wires
			block.type == Material.SPONGE -> addSpongeNode(key)

			// Merge node behavior
			block.type == Material.IRON_BLOCK -> MergeNode(this, x, y, z)
			block.type == Material.REDSTONE_BLOCK -> MergeNode(this, x, y, z)

			// Split power evenly
			block.customBlock == CustomBlocks.ALUMINUM_BLOCK -> SplitterNode(this, x, y, z)

			// Redstone controlled gate
			block.type.isRedstoneLamp -> GateNode(this, x, y, z)

			// Power flow meter
			block.type == Material.OBSERVER -> PowerFlowMeter(this, x, y, z)
		}
	}

	override fun processBlockRemoval(key: Long) {
		manager.scope.launch {
			val previousNode = nodes[key]

			nodes.remove(key)
			//TODO check for splits
		}
	}

	override fun processBlockAddition(key: Long, new: BlockSnapshot) {
		createNodeFromBlock(new)
		//TODO
	}

	fun removeNode(key: Long) {
		nodes.remove(key)
	}

	val tickExecutor = IntervalExecutor(20) {
		for ((key, extractor) in extractors.filterValues { it.transferableNeighbors.isNotEmpty() }) {
			extractor.startStep().step()
		}
	}

	override fun tick() {
		tickExecutor()
	}

	private fun collectPowerMultiblockEntities() {
		manager.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}

	private fun addSpongeNode(position: Long) {
		val neighbors = getNeighborNodes(position, nodes)

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
				neighbors.mapNotNullTo(spongeNeighbors) { (key, value) -> (value as? SpongeNode)?.let { key to value }  }

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
}
