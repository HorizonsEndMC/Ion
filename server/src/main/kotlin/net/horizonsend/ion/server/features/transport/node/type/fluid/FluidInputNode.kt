package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
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

	override fun buildRelations(position: BlockKey) {
		super.buildRelations(position)
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	var boundMultiblockEntity: FluidStoringEntity? = null

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 0
	}
}
