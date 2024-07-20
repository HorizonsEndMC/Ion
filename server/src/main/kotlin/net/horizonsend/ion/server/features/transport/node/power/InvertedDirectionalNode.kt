package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.head.SingleBranchHead
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class InvertedDirectionalNode(override val network: PowerNetwork) : SingleNode, StepHandler<PowerNetwork> {
	override var isDead: Boolean = false
	override var position: BlockKey by Delegates.notNull()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	constructor(network: PowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is EndRodNode) return false
		return node !is SourceNode<*>
	}

	override suspend fun getNextNode(head: SingleBranchHead<PowerNetwork>, entranceDirection: BlockFace): Pair<TransportNode, BlockFace>? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it.first) }
		.randomOrNull()


	override suspend fun handleHeadStep(head: SingleBranchHead<PowerNetwork>): StepResult<PowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}
}
