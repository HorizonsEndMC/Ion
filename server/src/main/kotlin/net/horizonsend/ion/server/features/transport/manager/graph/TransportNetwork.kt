package net.horizonsend.ion.server.features.transport.manager.graph

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.TransportNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.Chunk
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

@Suppress("UnstableApiUsage")
abstract class TransportNetwork<N: TransportNode>(val uuid: UUID, open val manager: NetworkManager<N, TransportNetwork<N>>) {
	var isAlive: Boolean = true; private set

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
	private val nodeMirror = ConcurrentHashMap<BlockKey, N>()
	protected val chunkMap = ConcurrentHashMap<Long, ConcurrentHashMap<BlockKey, N>>()

	fun getNodeAtLocation(location: BlockKey): N? = nodeMirror[location]

	fun isNodePresent(location: BlockKey): Boolean = nodeMirror.containsKey(location)

	fun getAllNodeLocations() = nodeMirror.keys

	fun setNodeMirror(location: BlockKey, node: N) {
		getChunkMirror(Chunk.getChunkKey(getX(location).shr(4), getZ(location).shr(4)))[location] = node
		nodeMirror[location] = node
	}

	fun removeNodeMirror(location: BlockKey): N? {
		getChunkMirror(Chunk.getChunkKey(getX(location).shr(4), getZ(location).shr(4))).remove(location)
		return nodeMirror.remove(location)
	}

	fun getChunkMirror(chunkKey: Long): ConcurrentHashMap<BlockKey, N> {
		return chunkMap.getOrPut(chunkKey) { ConcurrentHashMap<BlockKey, N>() }
	}

	fun getCoveredChunks() = chunkMap.keys

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
		if (isNodePresent(node.location)) throw IllegalStateException("Node already exists at ${toVec3i(node.location)} in graph!")
		if (getGraphNodes().any { eixsting -> eixsting.location == node.location }) throw IllegalStateException("Node already exists at ${toVec3i(node.location)} in graph!")

		manager.registerNode(node, this)

		setNodeMirror(node.location, node)

		networkGraph.addNode(node)

		val adjacentNodes = getAdjacent(node)
		for (connected in adjacentNodes) {
			val edgeOne = createEdge(connected, node)

			addEdge(node, connected, edgeOne)
			onEdgeConnected(edgeOne)

			val edgeTwo = createEdge(node, connected)

			addEdge(connected, node, edgeTwo)
			onEdgeConnected(edgeTwo)
		}

		onModified()
	}

	fun addNodes(nodes: Set<N>) {
		nodes.forEach(::addNode)
	}

	/** Gets all nodes adjacent to the provided node */
	fun getAdjacent(from: N): Set<N> = from.getPipableDirections().mapNotNullTo(mutableSetOf<N>()) { face -> getNodeAtLocation(getRelative(from.location, face))?.takeIf { node -> node.getPipableDirections().contains(face.oppositeFace) } }

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

		// Remove from the mirror first so that the manager can check if any nodes are present in its chunk to verify the lookup
		removeNodeMirror(node.location)

		manager.deRegisterNode(node, this)

		handleNodeRemoval()
	}

	fun removeNodes(nodes: Collection<N>) = localLock.writeLock().withLock {
		for (node in nodes) {
			networkGraph.removeNode(node)

			// Remove from the mirror first so that the manager can check if any nodes are present in its chunk to verify the lookup
			removeNodeMirror(node.location)

			manager.deRegisterNode(node, this)
		}

		handleNodeRemoval()
	}

	private fun handleNodeRemoval(): NodeRemovalResult {
		if (networkGraph.nodes().isEmpty()) {
			manager.removeNetwork(this)
			return NodeRemovalResult.RemovedNetwork
		}

		if (!manager.trySplitGraph(this)) {
			// If the graph was not split, it was only modified
			onModified()
			return NodeRemovalResult.RemovedSingle
		}

		return NodeRemovalResult.Split
	}

	fun handleChunkUnload(chunk: IonChunk) {
		val chunkMirror: ConcurrentHashMap<BlockKey, N> = getChunkMirror(chunk.locationKey)
		if (chunkMirror.isEmpty()) return

		for ((_, node) in chunkMirror) {
			handleNodeUnload(node)
		}

		removeNodes(chunkMirror.values)
	}

	open fun preSave() {}

	open fun handleNodeUnload(node: N) {}

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
		} catch (e: Throwable) {
			e.printStackTrace()
		}
		finally {
			isTicking = false
		}
	}

	protected abstract fun handleTick()

	open fun onModified() {}
	open fun onNodeAdded(node: N) {}
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

	protected fun discoverNetwork() {
		val visitQueue = ArrayDeque<BlockKey>()
		// A set is maintained to allow faster checks of
		val visitSet = LongOpenHashSet()

		visitQueue.addAll(getAllNodeLocations())
		visitSet.addAll(getAllNodeLocations())

		val visited = LongOpenHashSet()

		var tick = 0

		while (visitQueue.isNotEmpty() && tick < 10000 && isAlive) whileLoop@{
			tick++
			val key = visitQueue.removeFirst()
			val node = getNodeAtLocation(key) ?: continue
			visitSet.remove(key)

			visited.add(key)

			var toBreak = false

			for (face in node.getPipableDirections()) {
				val adjacent = getRelative(key, face)

				if (isNodePresent(adjacent)) continue
				if (visitSet.contains(adjacent) || visited.contains(adjacent)) continue

				val discoveryResult = manager.discoverPosition(adjacent, face, this)

				// Check the node here
				if (discoveryResult is NetworkManager.NodeRegistrationResult.Nothing) continue
				if (discoveryResult is NetworkManager.NodeRegistrationResult.CombinedGraphs) {
					toBreak = true
					break
				}

				visitQueue.add(adjacent)
			}

			if (toBreak) {
				break
			}
		}
	}
}
