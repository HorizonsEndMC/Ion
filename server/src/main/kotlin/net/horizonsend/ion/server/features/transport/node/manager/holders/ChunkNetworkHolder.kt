package net.horizonsend.ion.server.features.transport.node.manager.holders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.World
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.Delegates

class ChunkNetworkHolder<T: NodeManager> private constructor (val manager: ChunkTransportManager) : NetworkHolder<T> {
	override var network: T by Delegates.notNull(); private set

	constructor(manager: ChunkTransportManager, network: (ChunkNetworkHolder<T>) -> T) : this(manager) {
		this.network = network(this)
	}

	override val scope: CoroutineScope = manager.scope

	override fun getWorld(): World = manager.chunk.world

	override fun getGlobalNode(key: BlockKey): TransportNode? {
		val chunkX = getX(key).shr(4)
		val chunkZ = getZ(key).shr(4)

		val isThisChunk = chunkX == manager.chunk.x && chunkZ == manager.chunk.z

		if (isThisChunk) {
			return network.nodes[key]
		}

		val xDiff = manager.chunk.x - chunkX
		val zDiff = manager.chunk.z - chunkZ

		if (xDiff > 1 || xDiff < -1) return null
		if (zDiff > 1 || zDiff < -1) return null

		val chunk = IonChunk[getWorld(), chunkX, chunkZ] ?: return null
		return network.type.get(chunk).nodes[key]
	}

	override fun getInternalNode(key: BlockKey): TransportNode? {
		return network.nodes[key]
	}

	private val chunkPDC get() = manager.chunk.inner.persistentDataContainer
	private val networkPDC get() = chunkPDC.get(
		network.namespacedKey,
		PersistentDataType.TAG_CONTAINER
	) ?: run {
//		IonServer.slF4JLogger.warn("chunk ${manager.chunk.x}, ${manager.chunk.z} ${manager.chunk.world.name} didn't have transport information!")

		chunkPDC.adapterContext.newPersistentDataContainer()
	}

	override fun handleLoad() {
		// Load data sync
		val good = loadData()

		manager.scope.launch {
			val adapterContext = chunkPDC.adapterContext

			// Handle cases of data corruption
			if (!good) {
				network.clearData()
				collectAllNodes().join()

				// Save rebuilt data
				save(adapterContext)
			}

			network.buildRelations()
			network.finalizeNodes()
		}
	}

	override fun handleUnload() {
		network.onUnload()

		save(manager.chunk.inner.persistentDataContainer.adapterContext)
	}

	fun save(adapterContext: PersistentDataAdapterContext) {
		val container = adapterContext.newPersistentDataContainer()

		val serializedNodes: MutableMap<TransportNode, PersistentDataContainer> = mutableMapOf()

		network.nodes.values.distinct().forEach { node ->
			serializedNodes[node] = node.serialize(adapterContext, node)
		}

		if (serializedNodes.isNotEmpty()) {
			manager.chunk.inner.minecraft.isUnsaved = true
		}

		container.set(NamespacedKeys.NODES, PersistentDataType.TAG_CONTAINER_ARRAY, serializedNodes.values.toTypedArray())
		container.set(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, network.dataVersion)
		network.saveAdditional(container)

		// Save the network PDC
		chunkPDC.set(network.namespacedKey, PersistentDataType.TAG_CONTAINER, container)
	}

	/**
	 * Load stored node data from the chunk
	 *
	 * @return Whether the data was intact, or up to date
	 **/
	fun loadData(): Boolean {
		val version	= networkPDC.getOrDefault(NamespacedKeys.DATA_VERSION, PersistentDataType.INTEGER, 0)

		if (version < network.dataVersion) {
			IonServer.slF4JLogger.error("${manager.chunk}'s ${javaClass.simpleName} contained outdated data! It will be rebuilt")
			return false
		}

		// Deserialize once
		val nodeData = networkPDC.getOrDefault(NamespacedKeys.NODES, PersistentDataType.TAG_CONTAINER_ARRAY, arrayOf()).map {
			runCatching { TransportNode.load(it, network) }.onFailure {
				IonServer.slF4JLogger.error("${manager.chunk}'s ${javaClass.simpleName} contained corrupted data! It will be rebuilt")
				it.printStackTrace()
			}.getOrElse {
				return false
			}
		}

		nodeData.forEach { runCatching { it.loadIntoNetwork() }.onFailure {
			IonServer.slF4JLogger.error("${manager.chunk}'s ${javaClass.simpleName} loading node into network!")
			it.printStackTrace()
			return false
		} }

		return true
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
	private suspend fun collectSectionNodes(sectionY: Int) {
		val originX = manager.chunk.originX
		val originY = sectionY.shl(4) - manager.chunk.inner.world.minHeight
		val originZ = manager.chunk.originZ

		for (x: Int in 0..15) {
			val realX = originX + x

			for (y: Int in 0..15) {
				val realY = originY + y

				for (z: Int in 0..15) {
					val realZ = originZ + z

					network.createNodeFromBlock(manager.chunk.world.getBlockAt(realX, realY, realZ))
				}
			}
		}
	}
}
