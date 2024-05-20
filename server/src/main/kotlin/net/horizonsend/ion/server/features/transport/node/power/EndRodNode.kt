package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.PowerTransportStep
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.data.Directional
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class EndRodNode(override val network: ChunkPowerNetwork) : MultiNode<EndRodNode, EndRodNode> {
	constructor(network: ChunkPowerNetwork, origin: Long) : this(network) {
		positions.add(origin)
	}

	override val positions: MutableSet<Long> = LongOpenHashSet()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
	}

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

	override suspend fun handleStep(step: Step) {
		// This is not an origin node, so we can assume that it is not an origin step
		step as PowerTransportStep

		val next = getTransferableNodes()
			.filterNot { step.traversedNodes.contains(it) }
			.filterNot { step.previous.currentNode == it }
			.firstOrNull() ?: return

		// Simply move on to the next node
		PowerTransportStep(
			step.origin,
			step.steps,
			next,
			step,
			step.traversedNodes
		).invoke()
	}

	override fun toString(): String = "(END ROD NODE: ${positions.size} positions, Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes)"
}
