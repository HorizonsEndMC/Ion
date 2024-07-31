package net.horizonsend.ion.server.features.transport.node.general

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
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

abstract class JunctionNode<T: TransportNetwork, A: JunctionNode<T, B, A>, B: JunctionNode<T, A, B>>(override val network: T) : MultiNode<B, A>, StepHandler<T> {
	override var isDead: Boolean = false
	override val positions: MutableSet<Long> = LongOpenHashSet()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode<*>
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
	}

	abstract suspend fun addBack(position: BlockKey)

	override suspend fun rebuildNode(position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			// Do not handle relations
			addBack(it)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			network.nodes[it]?.buildRelations(it)
		}
	}

	override suspend fun handleHeadStep(head: SingleBranchHead<T>): StepResult<T> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getNextNode(head: SingleBranchHead<T>, entranceDirection: BlockFace): Pair<TransportNode, BlockFace>? = getTransferableNodes()
		.filterNot { head.previousNodes.contains(it.first) }
		.firstOrNull()

//	override fun toString(): String = "(SPONGE NODE: ${positions.size} positions, Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes)"

	override fun toString(): String {
		return """
			[Gas Junction Node]
			${positions.size} positions
			Relationships: ${relationships.joinToString { it.sideTwo.toString() }}
		""".trimIndent()
	}
}
