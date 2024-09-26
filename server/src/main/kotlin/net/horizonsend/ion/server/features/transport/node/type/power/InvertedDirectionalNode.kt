package net.horizonsend.ion.server.features.transport.node.type.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class InvertedDirectionalNode(override val manager: PowerNodeManager) : SingleNode(), PowerPathfindingNode {
	override val type: NodeType = NodeType.POWER_INVERSE_DIRECTIONAL_NODE

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is EndRodNode) return false
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}

	override fun getNextNodes(previous: TransportNode): ArrayDeque<TransportNode> {
		// Since this is a single node, and the previous node must be transferable to this, it can't be a sponge.
		// So there will likely only be a single relation to this
		val direction = previous.getRelationshipWith(this).values
		if (direction.isEmpty()) return cachedTransferable // just in case
		val face = direction.first().offset

		getForwardTransferable(face)?.let { return ArrayDeque(listOf(it)) }

		return cachedTransferable
	}

	fun getForwardTransferable(incoming: BlockFace): TransportNode? = relationHolder.getAllOthers().firstOrNull {
		it.offset == incoming.oppositeFace && it.canTransfer
	}?.other
}
