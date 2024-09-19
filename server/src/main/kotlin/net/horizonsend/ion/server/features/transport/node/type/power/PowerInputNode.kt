package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class PowerInputNode(override val manager: PowerNodeManager) : SingleNode() {
	override val type: NodeType = NodeType.POWER_INPUT_NODE
	constructor(network: PowerNodeManager, position: BlockKey) : this(network) {
		this.position = position
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		return false
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	var boundMultiblockEntity: PoweredMultiblockEntity? = null

	fun isCalling(): Boolean = boundMultiblockEntity != null && !(boundMultiblockEntity?.storage?.isFull() ?: false)

	override fun onPlace(position: BlockKey) {
		super.onPlace(position)

//		getNearbyPowerInputs().forEach { manager.tryBindPowerNode(it) }
	}

//	fun getNearbyPowerInputs(): Collection<PoweredMultiblockEntity> {
//		return ADJACENT_BLOCK_FACES.mapNotNullTo(mutableSetOf()) {
//			manager.holder.getMultiblockManager()[getRelative(position, it)] as? PoweredMultiblockEntity
//		}
//	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}

	override fun toString(): String = "POWER INPUT NODE. Bound to $boundMultiblockEntity"
}
