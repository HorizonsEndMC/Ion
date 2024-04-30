package net.horizonsend.ion.server.features.transport.grid

import com.manya.pdc.base.MapDataType
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.Consolidatable
import net.horizonsend.ion.server.features.transport.node.nodes.TransportNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODES
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_LOCATIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.POWER_TRANSPORT
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.seconds
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

abstract class TransportNetwork(val manager: ChunkTransportManager) {
	val nodes: ConcurrentHashMap<Long, TransportNode> = ConcurrentHashMap()
	val world get() = manager.chunk.world

	val pdc get() = manager.chunk.inner.persistentDataContainer

	protected abstract val namespacedKey: NamespacedKey

// val grids: Nothing = TODO("TransportNetwork system")

	init {
	    loadData()
	}

	open fun setup() {}

	/**
	 * Handle the creation / loading of the node into memory
	 *
	 * Inheritors may choose to save persistent data, or not
	 **/
	abstract fun createNodeFromBlock(block: BlockSnapshot)

	abstract fun processBlockRemoval(key: Long)
	abstract fun processBlockAddition(key: Long, new: BlockSnapshot)

	companion object {
		private val locationDataType = MapDataType(
			Collectors.toMap({ it.key }, { it.value }),
			PersistentDataType.LONG,
			PersistentDataType.INTEGER
		)
	}

	/**
	 * Load stored node data from the chunk
	 **/
	private fun loadData() {
		val existing = pdc.get(POWER_TRANSPORT, PersistentDataType.TAG_CONTAINER) ?: return

		val nodeLocations = existing.get(NODE_LOCATIONS, locationDataType)!!
		val nodeData = existing.get(NODES, PersistentDataType.TAG_CONTAINER_ARRAY)!!.map { TransportNode.fromPrimitive(it, pdc.adapterContext) }

		for ((locationKey, nodeIndex) in nodeLocations) {
			nodes[locationKey] = nodeData[nodeIndex]
		}
	}

	fun save(adapterContext: PersistentDataAdapterContext) {
		val container = adapterContext.newPersistentDataContainer()

		val serializedNodes = nodes.values.associateWith { nodes.values.indexOf(it) to it.serialize(adapterContext, it) }
		//TODO find better implementation of storage

		val dataMap = nodes.map { (key, node) ->
			key to serializedNodes[node]!!.first
		}.toMap()

		container.set(NODE_LOCATIONS, locationDataType, dataMap)
		container.set(NODES, PersistentDataType.TAG_CONTAINER_ARRAY, serializedNodes.values.seconds().toTypedArray())

		pdc.set(namespacedKey, PersistentDataType.TAG_CONTAINER, container)

		saveAdditional()
	}

	open fun saveAdditional() {}

	/**
	 *
	 **/
	abstract fun tick()

	/**
	 * Builds the transportNetwork TODO better documentation
	 **/
	fun build() = manager.scope.launch {
		collectAllNodes().join()
		collectNeighbors()
		finalizeNodes()
		buildGraph()
	}

	/**
	 *
	 **/
	private fun collectAllNodes(): Job = manager.scope.launch {
		// Parallel collect the nodes of each section
		manager.chunk.sections.map { (y, _) ->
			launch { collectSectionNodes(y) }
		}.joinAll()
	}

	/**
	 * Collect all nodes in this chunk section
	 *
	 * Iterate the section for possible nodes, handle creation
	 **/
	suspend fun collectSectionNodes(sectionY: Int) {
		val originX = manager.chunk.originX
		val originY = sectionY.shl(4) - manager.chunk.inner.world.minHeight
		val originZ = manager.chunk.originZ

		for (x: Int in 0..15) {
			val realX = originX + x

			for (y: Int in 0..15) {
				val realY = originY + y

				for (z: Int in 0..15) {
					val realZ = originZ + z

					val snapshot = getBlockSnapshotAsync(manager.chunk.world, realX, realY, realZ) ?: continue

					createNodeFromBlock(snapshot)
				}
			}
		}
	}

	/**
	 * Get the neighbors of a node
	 **/
	private fun collectNeighbors() {
//		nodes.values.forEach { node -> node.collectNeighbors() }
	}

	/**
	 * Consolidates network nodes where possible
	 *
	 * e.g. a straight section may be represented as a single node
	 **/
	private fun finalizeNodes() {
		nodes.forEach { (_, node) ->
			if (node is Consolidatable) node.consolidate()
		}
	}

	/**
	 *
	 **/
	private fun buildGraph() {
		//TODO
	}

	fun getNode(x: Int, y: Int, z: Int): TransportNode? {
		val key = toBlockKey(x, y, z)
		return nodes[key]
	}
}
