package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SOLAR_CELL_COUNT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.INTEGER
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY

/**
 * Represents a solar panel, or multiple
 **/
class SolarPanelNode : MultiNode {
	/** The number of solar cells contained in this node */
	private var cellNumber: Int = 1
	override val positions: MutableSet<BlockKey> = LongOpenHashSet()

	override val transferableNeighbors: MutableSet<TransportNode> = ObjectOpenHashSet()

	override fun isTransferable(position: Long, node: TransportNode): Boolean {
		TODO("Not yet implemented")
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(SOLAR_CELL_COUNT, INTEGER, cellNumber)
		persistentDataContainer.set(NODE_COVERED_POSITIONS, LONG_ARRAY, positions.toLongArray())
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		cellNumber = persistentDataContainer.getOrDefault(SOLAR_CELL_COUNT, INTEGER, 1)
		val coveredPositions = persistentDataContainer.get(NODE_COVERED_POSITIONS, LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }
	}

	override suspend fun rebuildNode(network: ChunkTransportNetwork, position: BlockKey) {
		// Create new nodes, automatically merging together
		positions.forEach {
			val node = PowerNodeFactory.addSolarPanel(network as ChunkPowerNetwork, it)
			network.nodes[it] = node
		}
	}

	private var lastTicked: Long = System.currentTimeMillis()

	/**
	 * Returns the amount of power between ticks
	 **/
	fun getPower(): Int {
		val time = System.currentTimeMillis()
		val diff = time - this.lastTicked

		return ((diff / 1000.0) * POWER_PER_SECOND).toInt() * cellNumber
	}

	companion object {
		const val POWER_PER_SECOND = 50
	}
}
