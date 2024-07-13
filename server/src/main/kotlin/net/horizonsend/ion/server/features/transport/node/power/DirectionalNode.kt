package net.horizonsend.ion.server.features.transport.node.power

import com.manya.pdc.base.EnumDataType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
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
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class DirectionalNode(override val network: ChunkPowerNetwork) : SingleNode, StepHandler<ChunkPowerNetwork> {
	override var isDead: Boolean = false
	override var position: BlockKey by Delegates.notNull()
	private var variant: Material by Delegates.notNull()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	constructor(network: ChunkPowerNetwork, position: BlockKey, variant: Material) : this(network) {
		this.position = position
		this.variant = variant
	}

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override suspend fun getNextNode(head: SingleBranchHead<ChunkPowerNetwork>, entranceDirection: BlockFace): Pair<TransportNode, BlockFace>? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it.first) }
		.randomOrNull()


	override suspend fun handleHeadStep(head: SingleBranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
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
}
