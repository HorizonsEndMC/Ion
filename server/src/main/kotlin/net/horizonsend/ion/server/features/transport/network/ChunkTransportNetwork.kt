package net.horizonsend.ion.server.features.transport.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.NodeFactory
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.DATA_VERSION
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.seconds
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.ConcurrentHashMap

abstract class ChunkTransportNetwork(val manager: ChunkTransportManager) {
	val nodes: ConcurrentHashMap<Long, TransportNode> = ConcurrentHashMap()
	val extractors: ConcurrentHashMap<Long, PowerExtractorNode> = ConcurrentHashMap()

	val world get() = manager.chunk.world

	val pdc get() = manager.chunk.inner.persistentDataContainer

	protected abstract val namespacedKey: NamespacedKey
	protected abstract val type: NetworkType
	abstract val nodeFactory: NodeFactory<*>
	abstract val dataVersion: Int

	open fun setup() {}

	/**
	 * Handle the creation / loading of the node into memory
	 **/
	open suspend fun createNodeFromBlock(block: BlockSnapshot) {
		val key = toBlockKey(block.x, block.y, block.z)

		nodeFactory.create(key, block)
	}

	abstract fun processBlockRemoval(key: Long)
	abstract fun processBlockAddition(key: Long, new: BlockSnapshot)

	/**
	 * Load stored node data from the chunk
	 *
	 * @return Whether the data was intact, or up to date
	 **/
	fun loadData(): Boolean {
		val existing = pdc.get(namespacedKey, PersistentDataType.TAG_CONTAINER) ?: return false
		val version	= pdc.getOrDefault(DATA_VERSION, PersistentDataType.INTEGER, 0)

		if (version < dataVersion) {
			IonServer.slF4JLogger.error("${manager.chunk}'s ${javaClass.simpleName} contained outdated data! It will be rebuilt")
			return false
		}

		// Deserialize once
		val nodeData = existing.getOrDefault(NODES, PersistentDataType.TAG_CONTAINER_ARRAY, arrayOf()).mapNotNull {
			runCatching { TransportNode.load(it, this) }.onFailure {
				IonServer.slF4JLogger.error("${manager.chunk}'s ${javaClass.simpleName} contained corrupted data! It will be rebuilt")
				it.printStackTrace()
			}.getOrElse { return false }
		}

		nodeData.forEach { runCatching { it.loadIntoNetwork() }.onFailure {
			IonServer.slF4JLogger.error("${manager.chunk}'s ${javaClass.simpleName} loading node into network!")
			it.printStackTrace()
		}.onFailure { return false } }

		return true
	}

	fun save(adapterContext: PersistentDataAdapterContext) {
		val container = adapterContext.newPersistentDataContainer()

		val serializedNodes: MutableMap<TransportNode, Pair<Int, PersistentDataContainer>> = mutableMapOf()

		nodes.forEach { (_, node) ->
			serializedNodes[node] = nodes.values.indexOf(node) to node.serialize(adapterContext, node)
		}

		container.set(NODES, PersistentDataType.TAG_CONTAINER_ARRAY, serializedNodes.values.seconds().toTypedArray())

		pdc.set(namespacedKey, PersistentDataType.TAG_CONTAINER, container)

		saveAdditional(pdc)
	}

	open fun saveAdditional(pdc: PersistentDataContainer) {}

	/**
	 *
	 **/
	abstract suspend fun tick()

	abstract suspend fun clearData()

	/**
	 * Logic for when the holding chunk is unloaded
	 **/
	fun onUnload() {
		// Break cross chunk relations
		breakAllRelations()

		save(manager.chunk.inner.persistentDataContainer.adapterContext)
	}

	/**
	 * Builds the transportNetwork
	 *
	 * Existing data will be loaded from the chunk's persistent data container, relations between nodes will be built, and any finalization will be performed
	 **/
	fun build() = manager.scope.launch {
		if (!loadData()) {
			clearData()
			collectAllNodes().join()
		}

		// Save rebuilt data
		save(manager.chunk.inner.persistentDataContainer.adapterContext)

		buildRelations()
		finalizeNodes()
	}

	/**
	 * Build node data from an unregistered state
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
	suspend fun buildRelations() {
		for ((key, node) in nodes) {
			node.buildRelations(key)
		}
	}

	/**
	 * Handles any cleanup tasks at the end of loading
	 **/
	open fun finalizeNodes() {}

	fun breakAllRelations() {
		nodes.values.forEach { it.clearRelations() }
	}

	fun getNode(x: Int, y: Int, z: Int): TransportNode? {
		val key = toBlockKey(x, y, z)
		return nodes[key]
	}

	/**
	 * Gets a node from this chunk, or a direct neighbor, if loaded
	 **/
	fun getNode(key: BlockKey, allowNeighborChunks: Boolean = true): TransportNode? {
		val chunkX = getX(key).shr(4)
		val chunkZ = getZ(key).shr(4)

		val isThisChunk = chunkX == manager.chunk.x && chunkZ == manager.chunk.z

		if (!allowNeighborChunks && isThisChunk) return null

		if (isThisChunk) {
			return nodes[key]
		}

		val xDiff = manager.chunk.x - chunkX
		val zDiff = manager.chunk.z - chunkZ

		if (xDiff > 1 || xDiff < -1) return null
		if (zDiff > 1 || zDiff < -1) return null

		val chunk = IonChunk[world, chunkX, chunkZ] ?: return null
		return type.get(chunk).nodes[key]
	}
}
