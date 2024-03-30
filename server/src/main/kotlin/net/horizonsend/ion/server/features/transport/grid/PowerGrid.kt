package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.features.transport.node.general.GateNode
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.power.MergeNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.SplitterNode
import net.horizonsend.ion.server.miscellaneous.utils.IntervalExecutor
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.isRedstoneLamp
import org.bukkit.Material
import org.bukkit.block.data.Directional
import java.util.concurrent.ConcurrentHashMap

class PowerGrid(network: ChunkTransportNetwork) : Grid(network) {
	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()
	val extractors = ConcurrentHashMap<Long, PowerExtractorNode>()

	override fun setup() {
		collectPowerMultiblockEntities()
	}

	override fun loadNode(block: BlockSnapshot): GridNode? {
		val x = block.x
		val y = block.y
		val z = block.z

		return when {
			// Extract power from storage
			block.type == Material.CRAFTING_TABLE -> PowerExtractorNode(this, x, y, z).apply { extractors[toBlockKey(x, y, z)] = this }

			// Add power to storage
			block.type == Material.NOTE_BLOCK -> PowerInputNode(this, x, y, z)

			// Straight wires
			block.type == Material.END_ROD -> LinearNode(this, x, y, z, block.data as Directional)

			// Omnidirectional wires
			block.type == Material.SPONGE -> JunctionNode(this, x, y, z)

			// Merge node behavior
			block.type == Material.IRON_BLOCK -> MergeNode(this, x, y, z)
			block.type == Material.REDSTONE_BLOCK -> MergeNode(this, x, y, z)

			// Split power evenly
			block.customBlock == CustomBlocks.ALUMINUM_BLOCK -> SplitterNode(this, x, y, z)

			// Redstone controlled gate
			block.type.isRedstoneLamp -> GateNode(this, x, y, z)

			// Power flow meter
			block.type == Material.OBSERVER -> PowerFlowMeter(this, x, y, z)
			else -> null
		}
	}

	override fun processBlockChange(key: Long, new: BlockSnapshot) {
		val newNode = loadNode(new)

		val previousNode = nodes[key]

		if (previousNode == null) {
			nodes[key] = newNode ?: return
			newNode.collectNeighbors()
			newNode.notifyNeighbors()

			return
		}

		if (newNode == null) {
			removeNode(key)
			return
		}

		previousNode.replace(newNode)
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
		network.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}
}
