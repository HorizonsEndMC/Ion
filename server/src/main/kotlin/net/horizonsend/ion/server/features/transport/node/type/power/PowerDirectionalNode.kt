package net.horizonsend.ion.server.features.transport.node.type.power

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerDirectionalNode(override val manager: PowerNodeManager) : SingleNode(), PowerPathfindingNode {
	override val type: NodeType = NodeType.POWER_DIRECTIONAL_NODE
	private var variant: Material by Delegates.notNull()

	constructor(network: PowerNodeManager, position: BlockKey, variant: Material) : this(network) {
		this.position = position
		this.variant = variant
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is SpongeNode) return false
		return node !is PowerExtractorNode && node !is SolarPanelNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
		persistentDataContainer.set(NamespacedKeys.NODE_VARIANT, materialDataType, variant)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
		variant = persistentDataContainer.get(NamespacedKeys.NODE_VARIANT, materialDataType)!!
	}

	companion object {
		val materialDataType = EnumDataType(Material::class.java)
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}

	override fun getNextNodes(previous: TransportNode): ArrayDeque<TransportNode> {
		// Since this is a single node, and the previous node must be transferable to this, it can't be a sponge.
		// So there will likely only be a single relation to this
		val direction = previous.getRelationshipWith(this).values
		if (direction.isEmpty()) return cachedTransferable
		val face = direction.first().offset

		getForwardTransferable(face)?.let { return ArrayDeque(listOf(it)) }

		return cachedTransferable
	}

	fun getForwardTransferable(incoming: BlockFace): TransportNode? = relationships.values.firstOrNull { it.offset == incoming.oppositeFace && it.canTransfer }?.other

	override fun toString(): String {
		val face = ADJACENT_BLOCK_FACES.random()
		return "directional: $face is ${getForwardTransferable(face)}, $cachedTransferable"
	}
}
