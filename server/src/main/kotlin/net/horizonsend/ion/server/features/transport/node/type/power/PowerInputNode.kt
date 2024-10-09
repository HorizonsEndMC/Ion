package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.data.type.WallSign
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PowerInputNode(override val manager: PowerNodeManager) : SingleNode(), PowerPathfindingNode {
	override val type: NodeType = NodeType.POWER_INPUT_NODE

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	fun isCalling(): Boolean {
		val entities = getPoweredEntities()
		if (entities.isEmpty()) return false
		return entities.any { it.powerStorage.getRemainingCapacity() > 0 }
	}

	fun getPoweredEntities(): Collection<PoweredMultiblockEntity> {
		return ADJACENT_BLOCK_FACES.mapNotNullTo(mutableListOf()) {
			val relativeKey = getRelative(position, it)
			val entity = MultiblockEntities.getMultiblockEntity(manager.world, relativeKey) as? PoweredMultiblockEntity
			if (entity != null) return@mapNotNullTo entity

			val data = getBlockDataSafe(manager.world, getX(relativeKey), getY(relativeKey), getZ(relativeKey)) ?: return@mapNotNullTo null
			if (!data.material.isWallSign) return@mapNotNullTo  null
			data as WallSign

			val originKey = getRelative(relativeKey, data.facing.oppositeFace)
			MultiblockEntities.getMultiblockEntity(manager.world, originKey) as? PoweredMultiblockEntity
		}
	}

	fun distributePower(power: Int): Int {
		val entities = getPoweredEntities().filterTo(mutableListOf()) { !it.powerStorage.isFull() }

		if (entities.isEmpty()) return power

		// Skip math for most scenarios
		if (entities.size == 1) return entities.first().powerStorage.addPower(power)

		var remainingPower = power

		while (remainingPower > 0) {
			if (entities.isEmpty()) break

			val share = remainingPower / entities.size
			val minRemaining = entities.minOf { it.powerStorage.getRemainingCapacity() }
			val distributed = minOf(minRemaining, share)

			val iterator = entities.iterator()
			while (iterator.hasNext()) {
				val entity = iterator.next()

				val r = entity.powerStorage.addPower(distributed)
				if (entity.powerStorage.isFull()) iterator.remove()

				remainingPower -= (distributed - r)
			}
		}

		return remainingPower
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?): ArrayDeque<TransportNode> {
		return ArrayDeque(0) // Can't transfer to anything
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		return false
	}

	override fun toString(): String = "POWER INPUT NODE. Bound to ${getPoweredEntities().joinToString { it.toString() }}"
}
