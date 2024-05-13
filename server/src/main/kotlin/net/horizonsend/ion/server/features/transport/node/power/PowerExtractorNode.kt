package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.PowerOriginStep
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.TransportStep
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerExtractorNode(override val network: ChunkPowerNetwork) : SingleNode, SourceNode {
	// The position will always be set
	override var position by Delegates.notNull<Long>()

	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
		network.extractors[position] = this
	}

	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()
	val extractableNodes: MutableSet<PowerInputNode> get() = relationships.mapNotNullTo(mutableSetOf()) { it.sideTwo.node as? PowerInputNode }

	val useful get() = extractableNodes.size >= 1

	override fun isTransferableTo(node: TransportNode): Boolean {
		if (node is PowerInputNode) return false
		return node !is SourceNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		network.extractors[position] = this
	}

	override suspend fun handleRemoval(position: BlockKey) {
		network.extractors.remove(position)
		super.handleRemoval(position)
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.getNode(offsetKey) ?: continue

			if (this == neighborNode) return

			if (neighborNode is PowerInputNode) {
				extractableNodes.add(neighborNode)
			}

			// Add a relationship, if one should be added
			addRelationship(neighborNode)
		}
	}

	override suspend fun handleStep(step: Step) {
		// Nothing can transfer to extractors
		step as PowerOriginStep

		val next = getTransferableNodes().randomOrNull() ?: return

		// Simply move on to the next node
		TransportStep(step, step.steps, next, step, step.traversedNodes).invoke()
	}

	override fun toString(): String = "POWER Extractor NODE: Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes"
}

