package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.data.Directional
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class EndRodNode() : MultiNode {
	constructor(origin: Long) : this() {
		positions.add(origin)
	}

	override val positions: MutableSet<Long> = LongOpenHashSet()

	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	override fun isTransferable(position: Long, node: TransportNode): Boolean {
		return true
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NamespacedKeys.NODE_COVERED_POSITIONS, PersistentDataType.LONG_ARRAY, positions.toLongArray())
	}

	override suspend fun rebuildNode(network: ChunkTransportNetwork, position: BlockKey) {

		// Create new nodes, automatically merging together
		positions.forEach {
			val node = PowerNodeFactory.getNodeForPosition(network as ChunkPowerNetwork, getBlockSnapshotAsync(network.world, it)!!.data as Directional, it)
			network.nodes[it] = node
		}
	}
}
