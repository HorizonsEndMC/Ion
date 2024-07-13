package net.horizonsend.ion.server.features.transport.node.power

import com.manya.pdc.base.EnumDataType
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.head.BranchHead
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.faces
import org.bukkit.Axis
import org.bukkit.block.data.Directional
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class EndRodNode(override val network: ChunkPowerNetwork) : MultiNode<EndRodNode, EndRodNode>, StepHandler<ChunkPowerNetwork> {
	constructor(network: ChunkPowerNetwork, origin: Long, axis: Axis) : this(network) {
		positions.add(origin)
		this.axis = axis
	}

	override var isDead: Boolean = false
	override val positions: MutableSet<Long> = LongOpenHashSet()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()
	var axis by Delegates.notNull<Axis>()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override suspend fun handleHeadStep(head: BranchHead<ChunkPowerNetwork>): StepResult<ChunkPowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: BranchHead<ChunkPowerNetwork>): TransportNode? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it) }
		.firstOrNull()

	override suspend fun rebuildNode(position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			network.nodeFactory.addEndRod(getBlockSnapshotAsync(network.world, it)!!.data as Directional, it, handleRelationships = false)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			network.nodes[it]?.buildRelations(it)
		}
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in axis.faces.toList()) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.getNode(offsetKey) ?: continue

			if (this == neighborNode) continue

			addRelationship(neighborNode, offset)
		}
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }

		axis = persistentDataContainer.getOrDefault(NamespacedKeys.AXIS, EnumDataType(Axis::class.java), Axis.Y)
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
		persistentDataContainer.set(NamespacedKeys.AXIS, EnumDataType(Axis::class.java), axis)
	}

	override fun toString(): String = "(END ROD NODE: Axis: $axis; ${positions.size} positions; Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes)"
}
