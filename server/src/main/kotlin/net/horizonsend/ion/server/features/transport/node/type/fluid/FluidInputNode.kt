package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.data.type.WallSign
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class FluidInputNode(override val manager: FluidNodeManager) : SingleNode() {
	override val type: NodeType = NodeType.FLUID_INPUT

	constructor(network: FluidNodeManager, position: BlockKey) : this(network) {
		this.position = position
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		return false
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}

	fun getEntities(): Collection<FluidStoringEntity> {
		return ADJACENT_BLOCK_FACES.mapNotNullTo(mutableListOf()) {
			val relativeKey = getRelative(position, it)
			val entity = MultiblockEntities.getMultiblockEntity(manager.world, relativeKey) as? FluidStoringEntity
			if (entity != null) return@mapNotNullTo entity

			val data = getBlockDataSafe(manager.world, getX(relativeKey), getY(relativeKey), getZ(relativeKey)) ?: return@mapNotNullTo null
			if (!data.material.isWallSign) return@mapNotNullTo  null
			data as WallSign

			val originKey = getRelative(relativeKey, data.facing.oppositeFace)
			MultiblockEntities.getMultiblockEntity(manager.world, originKey) as? FluidStoringEntity
		}
	}

	fun canConsume(resource: PipedFluid) {

	}

	fun isCalling(): Boolean {
		val entities = getEntities()
		if (entities.isEmpty()) return false
		return entities.any { !it.isFull() }
	}
}
