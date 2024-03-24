package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.features.transport.node.general.JunctionNode
import net.horizonsend.ion.server.features.transport.node.general.LinearNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.miscellaneous.utils.isRedstoneLamp
import org.bukkit.Material
import org.bukkit.block.data.Directional
import java.util.concurrent.ConcurrentHashMap

class PowerGrid(network: ChunkTransportNetwork) : Grid(network) {
	private val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()

	override fun setup() {
		collectPowerMultiblockEntities()
	}

	override fun isNode(block: BlockSnapshot): Boolean = when (block.type) {
		Material.END_ROD, Material.SPONGE, Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> true
		else -> false
	}

	override fun loadNode(block: BlockSnapshot): GridNode? {
		val x = block.x
		val y = block.y
		val z = block.z

		return when {
			// Extract power from storage
			block.type == Material.CRAFTING_TABLE -> PowerExtractorNode(this, x, y, z)

			// Add power to storage
			block.type == Material.NOTE_BLOCK -> PowerInputNode(this, x, y, z)

			// Straight wires
			block.type == Material.END_ROD -> LinearNode(this, x, y, z, block.data as Directional)

			// Omnidirectional wires
			block.type == Material.SPONGE -> JunctionNode(this, x, y, z)

			// Merge node behavior
			block.type == Material.IRON_BLOCK -> MergeNode()
			block.type == Material.REDSTONE_BLOCK -> MergeNode()

			// Split power evenly
			block.customBlock == CustomBlocks.ALUMINUM_BLOCK -> SplitterNode()

			// Redstone controlled gate
			block.type.isRedstoneLamp -> GateNode()

			// Power flow meter
			block.type == Material.OBSERVER -> PowerFlowMeter()
			else -> null
		}
	}

	override fun processBlockChange(previous: BlockSnapshot, new: BlockSnapshot) {

	}

	fun removeNode(key: Long) {

	}

	override fun tick() {
		TODO("Not yet implemented")
	}

	private fun collectPowerMultiblockEntities() {
		network.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}
}
