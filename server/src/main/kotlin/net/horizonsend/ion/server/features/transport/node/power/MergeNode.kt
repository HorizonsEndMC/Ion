package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.IntermediateNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class MergeNode(override val network: ChunkPowerNetwork) : SingleNode, IntermediateNode<ChunkPowerNetwork> {
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()
	override var position: BlockKey by Delegates.notNull()

	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override suspend fun handleHeadStep(head: BranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: BranchHead<ChunkPowerNetwork>): TransportNode? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it) }
		.randomOrNull()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}
}
