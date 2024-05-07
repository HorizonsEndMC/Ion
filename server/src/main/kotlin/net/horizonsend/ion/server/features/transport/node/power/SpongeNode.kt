package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Represents a sponge [omnidirectional pipe]
 *
 * Since there is no use in keeping the individual steps, all touching sponges are consolidated into a single node with multiple inputs / outputs, weighted evenly
 **/
class SpongeNode() : MultiNode<SpongeNode, SpongeNode> {
	constructor(origin: BlockKey) : this() {
		positions.add(origin)
	}

	override val positions: MutableSet<BlockKey> = LongOpenHashSet()

	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	override fun isTransferable(position: BlockKey, node: TransportNode): Boolean {
		return true
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
	}

	override suspend fun rebuildNode(network: ChunkTransportNetwork, position: BlockKey) {
		transferableNeighbors.clear()

		// Create new nodes, automatically merging together
		positions.forEach {
			buildRelations(network, it)
			(network as ChunkPowerNetwork).nodeFactory.addSponge(it)
		}
	}

	override fun toString(): String = """
		SPONGE NODE:
		${positions.size} positions,
		Transferable to: ${transferableNeighbors.joinToString { it.javaClass.simpleName }} nodes
	""".trimIndent()

}
