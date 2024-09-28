package net.horizonsend.ion.server.features.transport.node.type.fluid

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.NodeType.FLUID_DIRECTIONAL_NODE
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.FluidNodeManager
import net.horizonsend.ion.server.features.transport.node.type.general.DirectionalNode
import net.horizonsend.ion.server.features.transport.node.type.general.UnTransferableNode
import net.horizonsend.ion.server.features.transport.node.type.power.PowerDirectionalNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataContainer
import kotlin.properties.Delegates

class FluidDirectionalNode(override val manager: FluidNodeManager) : DirectionalNode(), FluidPathfindingNode {
	override val type: NodeType = FLUID_DIRECTIONAL_NODE
	private var variant: Material by Delegates.notNull()

	constructor(network: FluidNodeManager, position: BlockKey, variant: Material) : this(network) {
		this.position = position
		this.variant = variant
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is UnTransferableNode) return false
		return node !is FluidJunctionNode
	}

	override fun getPathfindingResistance(previousNode: TransportNode?, nextNode: TransportNode?): Int {
		return 1
	}

	override fun getNextNodes(previous: TransportNode, destination: TransportNode?, resource: PipedFluid): ArrayDeque<TransportNode> {
		if (destination != null && relationHolder.hasRelationAtWith(position, destination)) return ArrayDeque(listOf(destination))

		// Since this is a single node, and the previous node must be transferable to this, it can't be a sponge.
		// So there will likely only be a single relation to this
		val direction = previous.getRelationshipWith(this).values
		if (direction.isEmpty()) return cachedTransferable
		val face = direction.first().offset

		getForwardTransferable(face)?.let { return ArrayDeque(listOf(it)) }

		return cachedTransferable
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		super.storeData(persistentDataContainer)
		persistentDataContainer.set(NamespacedKeys.NODE_VARIANT, PowerDirectionalNode.materialDataType, variant)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		super.loadData(persistentDataContainer)
		variant = persistentDataContainer.get(NamespacedKeys.NODE_VARIANT, PowerDirectionalNode.materialDataType)!!
	}

	override fun canTransfer(resource: PipedFluid): Boolean {
		return true
	}
}
