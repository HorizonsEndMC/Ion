package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.TransportStep
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Represents a sponge [omnidirectional pipe]
 *
 * Since there is no use in keeping the individual steps, all touching sponges are consolidated into a single node with multiple inputs / outputs, weighted evenly
 **/
class SpongeNode(override val network: ChunkPowerNetwork) : MultiNode<SpongeNode, SpongeNode> {
	constructor(network: ChunkPowerNetwork, origin: BlockKey) : this(network) {
		positions.add(origin)
	}

	override val positions: MutableSet<BlockKey> = LongOpenHashSet()

	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		return node !is SourceNode
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
	}

	override suspend fun rebuildNode(position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			// Do not handle relations
			network.nodeFactory.addSponge(it, handleRelationships = false)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			network.nodes[it]?.buildRelations(it)
		}
	}

	override suspend fun handleStep(step: Step) {
		// This is not an origin node, so we can assume that it is not an origin step
		step as TransportStep

		val next = getTransferableNodes()
			.filterNot { step.traversedNodes.contains(it) }
			.filterNot { step.previous.currentNode == it }
			.randomOrNull() ?: return

		println("Next node is $next")

		// Simply move on to the next node
		TransportStep(
			step.origin,
			step.steps,
			next,
			step,
			step.traversedNodes
		).invoke()
	}

	override fun toString(): String = "(SPONGE NODE: ${positions.size} positions, Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes)"
}
