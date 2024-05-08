package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class PowerExtractorNode(override val network: ChunkPowerNetwork) : SingleNode, SourceNode {
	constructor(network: ChunkPowerNetwork, position: BlockKey) : this(network) {
		this.position = position
	}

	override var position by Delegates.notNull<Long>()
	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	val extractableNodes: MutableSet<PowerInputNode> = ObjectOpenHashSet()

	val useful get() = extractableNodes.size >= 1

	override fun isTransferableTo(position: Long, node: TransportNode): Boolean {
		if (node is PowerInputNode) return false
		return node !is SourceNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG, position)
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		position = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG)!!
	}

	override suspend fun buildRelations(position: BlockKey) {
		for (offset in ADJACENT_BLOCK_FACES) {
			val offsetKey = getRelative(position, offset, 1)
			val neighborNode = network.nodes[offsetKey] ?: continue

			if (this == neighborNode) return

			if (neighborNode is PowerInputNode) {
				extractableNodes.add(neighborNode)
			}

			if (isTransferableTo(offsetKey, neighborNode)) {
				transferableNeighbors.add(neighborNode)
			}
		}
	}

	override fun toString(): String = """
		POWER INPUT NODE:
		Transferable to: ${transferableNeighbors.joinToString { it.javaClass.simpleName }} nodes
	""".trimIndent()
}

