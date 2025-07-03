package net.horizonsend.ion.server.features.transport.manager.graph

import net.horizonsend.ion.server.features.transport.manager.TransportHolder
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.LinkedList
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class NetworkManager<N : TransportNode, T: TransportNetwork<N>>(val transportManager: TransportHolder) {
	protected abstract val cacheFactory: BlockBasedCacheFactory<N, NetworkManager<N, T>>

	fun clear() {
		allNetworks.clear()
		graphUUIDLookup.clear()
		graphLocationLookup.clear()
	}

//	private val loadedGraphs = ConcurrentHashMap.newKeySet<T>()

	private val allNetworks = ConcurrentHashMap.newKeySet<T>()

	fun getAllGraphs() = allNetworks.toSet()

	private val graphUUIDLookup = ConcurrentHashMap<UUID, T>()

	fun getByUUID(uuid: UUID): T? = graphUUIDLookup[uuid]

	private val graphLocationLookup = ConcurrentHashMap<BlockKey, T>()

	fun getByLocation(location: BlockKey): T? = graphLocationLookup[location]

	fun allLocations() = graphLocationLookup.keys

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
		return network
	}

	fun removeNetwork(network: T) {
		network.setRemoved()

		allNetworks.remove(network)
		graphUUIDLookup.remove(network.uuid)
		network.positions.forEach { if (graphLocationLookup.remove(it) != network) throw IllegalStateException("Removed network was not at position ${toVec3i(it)}") }
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
	}

	/**
	 * Registers this node as part of the network
	 **/
	fun deRegisterNode(node: N) {
		graphLocationLookup.remove(node.location)
	}

	fun discoverPosition(location: BlockKey, discoveringNetwork: T): Boolean {
		val graph = getByLocation(location)

		// This should be checked ahead of time
		if (graph == discoveringNetwork) {
			throw IllegalStateException("Attempted to cache point inside itself. Imroperly removed? ${toVec3i(location)}")
		}

		if (graph == null) {
			return registerNewPosition(location)
		}

		// If this point is occupied by another graph, and they have not merged yet, merge them.
		combineGraphs(listOf(graph, discoveringNetwork))

		return true
	}

	fun registerNewPosition(location: BlockKey): Boolean {
		val graph = getByLocation(location)
		if (graph != null) {
			throw IllegalStateException("Attempted to cache point inside registered graph. Concurrent modification? ${toVec3i(location)}")
			return false
		}

		val node = createNode(location)
		if (node == null) return false

		// Check adjacent graphs to see if any are connected when this one is placed.
		val adjacentGraphs = ADJACENT_BLOCK_FACES.mapNotNullTo(mutableSetOf()) { getByLocation(getRelative(location, it)) }

		when {
			adjacentGraphs.isEmpty() -> createNewNetwork(node)
			adjacentGraphs.size == 1 -> adjacentGraphs.first().addNode(node)
			else -> combineGraphs(adjacentGraphs).addNode(node)
		}

		return true
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
			allNetworks.remove(toMerge)
			graphUUIDLookup.remove(toMerge.uuid)

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

	companion object {
		val FLUID_GRAPHS = NamespacedKeys.key("fluid_graphs")
	}

	fun tick() {
		getAllGraphs().forEach { t -> t.tick() }
	}
}

