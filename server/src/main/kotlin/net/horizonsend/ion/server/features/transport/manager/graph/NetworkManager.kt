package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.core.registration.keys.TransportNetworkNodeTypeKeys
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class NetworkManager<N : TransportNode, T: TransportNetwork<N>>(val transportManager: TransportHolder) {
	abstract val namespacedKey: NamespacedKey

	val referenceDirection = if (transportManager is ShipTransportManager) transportManager.starship.forward else BlockFace.NORTH

	protected abstract val cacheFactory: BlockBasedCacheFactory<N, NetworkManager<N, T>>

	fun clear() {
		allNetworks.clear()
		graphUUIDLookup.clear()
		graphLocationLookup.clear()
		graphChunkLookup.clear()
	}

	private val allNetworks = ConcurrentHashMap.newKeySet<T>()

	fun getAllGraphs() = allNetworks.toSet()

	private val graphUUIDLookup = ConcurrentHashMap<UUID, T>()

	fun getByUUID(uuid: UUID): T? = graphUUIDLookup[uuid]

	private val graphLocationLookup = ConcurrentHashMap<BlockKey, T>()

	fun getByLocation(location: BlockKey): T? = graphLocationLookup[location]

	fun allLocations() = graphLocationLookup.keys

	private val graphChunkLookup = ConcurrentHashMap<Long, ConcurrentHashMap.KeySetView<T, Boolean>>()

	fun getByChunkKey(chunkKey: BlockKey): MutableSet<T> = graphChunkLookup.getOrPut(chunkKey) { ConcurrentHashMap.newKeySet() }

	fun getByChunk(chunk: IonChunk): MutableSet<T> = graphChunkLookup.getOrPut(chunk.locationKey) { ConcurrentHashMap.newKeySet() }

	fun allOccupiedChunkKeys() = graphChunkLookup.keys

	fun registerNewNetwork(network: TransportNetwork<N>) {
		allNetworks.add(cast(network))
		graphUUIDLookup[network.uuid] = cast(network)
	}

	abstract fun networkProvider(): T

	fun createNewNetwork(): T {
		val network = networkProvider()
		registerNewNetwork(network)

		return network
	}

	fun createNewNetwork(node: N): T {
		val network = createNewNetwork()
		network.addNode(node)
		network.setReady()
		return network
	}

	fun removeNetwork(network: T) {
		network.setRemoved()

		removeNetworkRegistration(network)

		network.positions.forEach {
			val found = graphLocationLookup.remove(it)
			if (found != network) throw IllegalStateException("Removed network was not at position ${toVec3i(it)}! Expected $network, Found $found")
		}
	}

	fun removeNetworkRegistration(network: T) {
		allNetworks.remove(network)
		graphUUIDLookup.remove(network.uuid)

		for (chunk in network.getCoveredChunks()) {
			getByChunkKey(chunk).remove(network)
		}
	}

	fun createNode(block: Block): N? = cacheFactory.cache(block, this)
	fun createNode(key: BlockKey): N? {
		val block = getBlockIfLoaded(transportManager.getWorld(), getX(key), getY(key), getZ(key)) ?: return null
		return createNode(block)
	}

	/**
	 * Registers this node as part of the network
	 **/
	fun registerNode(node: N, network: T) {
		node.setNetworkOwner(network)
		graphLocationLookup[node.location] = network
		val chunkKey = Chunk.getChunkKey(getX(node.location).shr(4), getZ(node.location).shr(4))
		getByChunkKey(chunkKey).add(network)
	}

	/**
	 * Registers this node as part of the network
	 **/
	fun deRegisterNode(node: N, network: T) {
		graphLocationLookup.remove(node.location)
		val chunkKey = Chunk.getChunkKey(getX(node.location).shr(4), getZ(node.location).shr(4))
		val chunkNodes = network.getChunkMirror(chunkKey)
		if (chunkNodes.isEmpty()) getByChunkKey(chunkKey).remove(network)
	}

	sealed interface NodeRegistrationResult {
		data object Nothing : NodeRegistrationResult
		data class CreatedNew(val new: TransportNode) : NodeRegistrationResult
		data class CombinedGraphs(val graphs: Collection<TransportNetwork<*>>) : NodeRegistrationResult
	}

	fun discoverPosition(location: BlockKey, offset: BlockFace, discoveringNetwork: T): NodeRegistrationResult {
		val graph = getByLocation(location)

		// This should be checked ahead of time
		if (graph == discoveringNetwork) {
			throw IllegalStateException("Attempted to cache point inside itself. Imroperly removed? ${toVec3i(location)}")
		}

		if (graph == null) {
			return registerNewPosition(location) { it.getPipableDirections().contains(offset.oppositeFace) }
		}

		val nodeAtPosition = graph.getNodeAtLocation(location) ?: return NodeRegistrationResult.Nothing
		if (!nodeAtPosition.getPipableDirections().contains(offset)) return NodeRegistrationResult.Nothing

		// If this point is occupied by another graph, and they have not merged yet, merge them.
		val toCombine = listOf(graph, discoveringNetwork)

		combineGraphs(toCombine)
		return NodeRegistrationResult.CombinedGraphs(toCombine)
	}

	/**
	 * WARNING: Limited Use Only!
	 **/
	fun registerNewPosition(location: BlockKey, check: (N) -> Boolean = { true }): NodeRegistrationResult {
		val graph = getByLocation(location)
		if (graph != null) {
			throw IllegalStateException("Attempted to cache point inside registered graph. Concurrent modification? ${toVec3i(location)}")
		}

		val node = createNode(location)
		if (node == null) return NodeRegistrationResult.Nothing
		if (!check(node)) return NodeRegistrationResult.Nothing

		return registerNewNode(node)
	}

	fun registerNewNode(node: N): NodeRegistrationResult {
		// Check adjacent graphs to see if any are connected when this one is placed.
		val adjacentGraphs = node.getPipableDirections().mapNotNullTo(mutableSetOf()) { offset ->
			val position = getRelative(node.location, offset)

			getByLocation(position)?.takeIf { adjacent ->
				val node = adjacent.getNodeAtLocation(position) ?: return@takeIf false
				node.getPipableDirections().contains(offset.oppositeFace)
			}
		}

		return when {
			adjacentGraphs.isEmpty() -> {
				createNewNetwork(node)
				NodeRegistrationResult.CreatedNew(node)
			}
			adjacentGraphs.size == 1 -> {
				adjacentGraphs.first().addNode(node)
				NodeRegistrationResult.CreatedNew(node)
			}
			else -> {
				combineGraphs(adjacentGraphs)
				NodeRegistrationResult.CombinedGraphs(adjacentGraphs)
			}
		}
	}

	fun combineGraphs(graphs: Iterable<T>): T {
 		val sorted = graphs.sortedBy { it.getGraphNodes().size }

		val iterator = sorted.iterator()

		val mergeTraget = iterator.next()

		if (!iterator.hasNext()) return mergeTraget

		while (iterator.hasNext()) {
			val toMerge = iterator.next()
			if (toMerge === mergeTraget) throw IllegalStateException("Trying to merge grid with itself!")

			// Remove and de-register before trying to add its nodes to the merge target
			toMerge.setRemoved()
			removeNetworkRegistration(toMerge)

			mergeTraget.intakeNodes(toMerge);
			toMerge.onMergedInto(mergeTraget)
		}

		mergeTraget.onModified()

		return mergeTraget
	}

	fun trySplitGraph(oldGraph: TransportNetwork<N>): Boolean {
		// Generate the grid nodes isolated from each other.
		val splitGraphs: List<Set<N>> = separateGraphPositions(oldGraph)

		if (splitGraphs.size <= 1) return false

		removeNetwork(cast(oldGraph))

		// Create new nodes

		val newNetworks = splitGraphs.map { nodeSet ->
			val network = createNewNetwork()
			network.addNodes(nodeSet)
			network.setReady()
			network
		}

		oldGraph.onSplit(newNetworks)

		return true
	}

	/**
	 * Splits a multi node's positions into multiple nodes
	 * https://github.com/CoFH/ThermalDynamics/blob/1.20.x/src/main/java/cofh/thermal/dynamics/common/grid/GridContainer.java#L394
	 **/
	private fun separateGraphPositions(graph: TransportNetwork<N>): List<Set<N>> {
		val seen: MutableSet<N> = HashSet()
		val stack = LinkedList<N>()
		val separated: MutableList<Set<N>> = LinkedList()

		while (true) {
			var first: N? = null

			// Find next node in graph we haven't seen.
			for (node in graph.getGraphNodes()) {
				if (!seen.contains(node)) {
					first = node
					break
				}
			}

			// We have discovered all nodes, exit.
			if (first == null) break

			// Start recursively building out all nodes in this sub-graph
			val subGraph: MutableSet<N> = HashSet()

			stack.push(first)

			while (!stack.isEmpty()) {
				val entry = stack.pop()

				if (seen.contains(entry)) continue

				stack.addAll(graph.getAdjacentNodes(entry))
				seen.add(entry)
				subGraph.add(entry)
			}

			separated.add(subGraph)
		}

		return separated
	}

	// C : T? is to allow nullable or non-nullable clasts to T
	@Suppress("UNCHECKED_CAST")
	fun <C : T?> cast(graph: TransportNetwork<N>?): C = graph as C

	open fun save(adapterContext: PersistentDataAdapterContext) {
		val accumulated = mutableListOf<PersistentDataContainer>()

		for (graph in getAllGraphs()) {
			accumulated.add(graph.save(adapterContext))
		}

		transportManager.storePersistentData { store ->
			store.set(FLUID_GRAPHS, PersistentDataType.LIST.dataContainers(), accumulated)
		}
	}

	fun handleChunkUnload(chunk: IonChunk) {
		saveChunk(chunk)

		for (grid in getByChunk(chunk)) {
			grid.handleChunkUnload(chunk)
		}
	}

	fun saveChunk(chunk: IonChunk) {
		val nodeMap = ConcurrentHashMap<BlockKey, N>()

		for (grid in getByChunk(chunk)) {
			grid.preSave()
			nodeMap.putAll(grid.getChunkMirror(chunk.locationKey))
		}

		val nodes = nodeMap.values

		val chunkPDC = chunk.inner.persistentDataContainer
		val pdc = chunkPDC.adapterContext.newPersistentDataContainer()

		val locationArray = LongArray(nodes.size)
		val serializedNodes = arrayOfNulls<PersistentDataContainer>(nodes.size)

		for ((index, node) in nodes.withIndex()) {
			locationArray[index] = node.location
			serializedNodes[index] = node.type.serializeUnsafe(node, pdc.adapterContext)
		}

		pdc.set(NamespacedKeys.NODE_LOCATIONS, PersistentDataType.LONG_ARRAY, locationArray)

		@Suppress("UNCHECKED_CAST")
		val list = (serializedNodes as Array<PersistentDataContainer>).asList()

		pdc.set(NamespacedKeys.NODES, PersistentDataType.LIST.dataContainers(), list)

		chunkPDC.set(namespacedKey, PersistentDataType.TAG_CONTAINER, pdc)
	}

	companion object {
		val FLUID_GRAPHS = NamespacedKeys.key("fluid_graphs")
	}

	fun tick() {
		getAllGraphs().forEach { t -> t.tick() }
	}

	fun onChunkLoad(chunk: IonChunk) {
		val data = chunk.inner.persistentDataContainer.get(namespacedKey, PersistentDataType.TAG_CONTAINER) ?: return
		val nodes = data.get(NamespacedKeys.NODES, PersistentDataType.LIST.dataContainers()) ?: return

		for (serializedNode in nodes) {
			val type = serializedNode.get(NamespacedKeys.NODE_TYPE, TransportNetworkNodeTypeKeys.serializer)!!.getValue()
			val deserialized = type.deserialize(serializedNode, serializedNode.adapterContext)

			@Suppress("UNCHECKED_CAST")
			registerNewNode(deserialized as N)
		}
	}
}

