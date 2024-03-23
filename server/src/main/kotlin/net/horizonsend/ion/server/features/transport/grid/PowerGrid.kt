package net.horizonsend.ion.server.features.transport.grid

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.grid.node.GridNode
import net.horizonsend.ion.server.features.transport.grid.node.JunctionNode
import net.horizonsend.ion.server.features.transport.grid.node.StraightNode
import net.horizonsend.ion.server.features.transport.grid.node.power.PowerInputNode
import org.bukkit.Material
import java.util.concurrent.ConcurrentHashMap

class PowerGrid(network: ChunkTransportNetwork) : AbstractGrid(network) {
	private val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()

	override fun setup() {
		collectPowerMultiblockEntities()
	}

	override fun isNode(block: BlockSnapshot): Boolean = when (block.type) {
		Material.END_ROD, Material.SPONGE, Material.IRON_BLOCK, Material.REDSTONE_BLOCK -> true
		else -> false
	}

	override fun loadNode(block: BlockSnapshot): GridNode? = when (block.type) {
		Material.END_ROD -> StraightNode(this, block.x, block.y, block.z)
		Material.SPONGE -> JunctionNode(this, block.x, block.y, block.z)
		Material.IRON_BLOCK -> GrabbyNode()
		Material.REDSTONE_BLOCK -> GrabbyNode()
		Material.NOTE_BLOCK -> PowerExtractorNode()
		Material.CRAFTING_TABLE -> PowerInputNode()
		else -> null
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
