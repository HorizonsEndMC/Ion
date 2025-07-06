package net.horizonsend.ion.server.features.transport.manager.graph

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

@Suppress("UnstableApiUsage")
abstract class TransportNetwork<N: TransportNode>(val uuid: UUID, open val manager: NetworkManager<N, TransportNetwork<N>>) {
	private var removed: Boolean = false

	fun setRemoved() { removed = true }

	private var ready: Boolean = false

	fun setReady() { ready = true }

	/**
	 * Readwritelock for reading / modifying node configuration
	 **/
	private val localLock = ReentrantReadWriteLock()

	/**
	 * Keep track of nodes and their positions
	 **/
	val nodeMirror = ConcurrentHashMap<BlockKey, N>()

	val positions get() = nodeMirror.keys

	/**
	 * The inner graph containing the nodes and edges.
	 **/
	private val networkGraph: MutableNetwork<N, GraphEdge> = NetworkBuilder.directed()
		.allowsParallelEdges(false)
		.allowsSelfLoops(false)
		.build()

	fun getGraph() = localLock.readLock().withLock { networkGraph }

	fun getGraphNodes() = localLock.readLock().withLock { networkGraph.nodes() }

	fun getGraphEdges() = localLock.readLock().withLock { networkGraph.edges() }

	fun getAdjacentNodes(node: N) = localLock.readLock().withLock { networkGraph.adjacentNodes(node) }

	fun addNode(node: N) = localLock.writeLock().withLock {
		if (nodeMirror.containsKey(node.location)) throw IllegalStateException("Node already exists at ${toVec3i(node.location)} in graph!")
		if (getGraphNodes().any { eixsting -> eixsting.location == node.location }) throw IllegalStateException("Node already exists at ${toVec3i(node.location)} in graph!")

		manager.registerNode(node, this)

		nodeMirror[node.location] = node

		networkGraph.addNode(node)

		val adjacentNodes = getAdjacent(node)
		for (connected in adjacentNodes) {
			val edge = createEdge(connected, node)

			addEdge(node, connected, edge)
			onEdgeConnected(edge)
		}

		onModified()
	}

	fun addNodes(nodes: Set<N>) {
		nodes.forEach(::addNode)
	}

	/** Gets all nodes adjacent to the provided node */
	fun getAdjacent(from: N): Set<N> = from.getPipableDirections().mapNotNullTo(mutableSetOf<N>()) { face -> nodeMirror[getRelative(from.location, face)]?.takeIf { node -> node.getPipableDirections().contains(face.oppositeFace) } }

	abstract fun createEdge(nodeOne: N, nodeTwo: N): GraphEdge

	/**
	 * Adds an edge between the two nodes
	 **/
	private fun addEdge(nodeOne: N, nodeTwo: N, edge: GraphEdge) = localLock.writeLock().withLock {
		networkGraph.addEdge(nodeOne, nodeTwo, edge)
	}

	sealed interface NodeRemovalResult {
		data object RemovedSingle : NodeRemovalResult
		data object RemovedNetwork : NodeRemovalResult
		data object Split : NodeRemovalResult
	}

	/**
	 * Removes this node from the network
	 * May split the graph
	 **/
	fun removeNode(node: N): NodeRemovalResult = localLock.writeLock().withLock {
		networkGraph.removeNode(node)

		manager.deRegisterNode(node)

		nodeMirror.remove(node.location)

		if (networkGraph.nodes().isEmpty()) {
			manager.removeNetwork(this)
			return@withLock NodeRemovalResult.RemovedNetwork
		}

		if (!manager.trySplitGraph(this)) {
			// If the graph was not split, it was only modified
			onModified()
			return@withLock NodeRemovalResult.RemovedSingle
		}

		NodeRemovalResult.Split
	}

	/**
	 * Holds if the graph is currently executing a tick
	 **/
	var isTicking: Boolean = false; private set

	fun tick() {
		if (removed || !ready) return

		if (isTicking) return
		isTicking = true

		try {
			ensureNodeIntegrity()
			handleTick()
		} finally {
			isTicking = false
		}
	}

	protected abstract fun handleTick()

	open fun onModified() {}
	open fun onEdgeConnected(edge: GraphEdge) {}
	open fun onSplit(children: Collection<TransportNetwork<N>>) {}
	open fun onMergedInto(other: TransportNetwork<N>) {}

	/**
	 * Takes in the nodes from another graph, and adds them to this one.
	 **/
	@Suppress("UNCHECKED_CAST")
	fun intakeNodes(other : TransportNetwork<N>) {
		// Insert all nodes into the Grid's lookup maps and update the node about the grid change.
		for (node in other.getGraphNodes()) {
			addNode(node)
		}
	}

	fun ensureNodeIntegrity() {
		val missing = mutableSetOf<N>()

		for (node in getGraphNodes()) {
			val intact = node.isIntact() ?: continue
			if (intact) continue

			missing.add(node)
		}

		for (node in missing) {
			if (removeNode(node) is NodeRemovalResult.Split) break
		}
	}

	abstract fun save(adapterContext: PersistentDataAdapterContext): PersistentDataContainer
}
