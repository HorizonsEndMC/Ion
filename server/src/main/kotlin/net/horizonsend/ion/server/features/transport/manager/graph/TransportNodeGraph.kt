package net.horizonsend.ion.server.features.transport.manager.graph

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import github.scarsz.discordsrv.dependencies.alexh.Fluent
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import java.util.UUID
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

@Suppress("UnstableApiUsage")
abstract class TransportNodeGraph<T: GraphNode>(val uuid: UUID, open val manager: GraphManager<T, *>) {

	/**
	 * Keep track of nodes and their positions
	 **/
	val nodeMirror = Fluent.ConcurrentHashMap<BlockKey, T>()

	private val networkLock = ReentrantReadWriteLock()
	private val networkGraph: MutableNetwork<T, GraphEdge> = NetworkBuilder.directed()
		.allowsParallelEdges(false)
		.allowsSelfLoops(false)
		.build()

	fun getGraph() = networkLock.readLock().withLock { networkGraph }

	fun getGraphNodes() = networkLock.readLock().withLock { networkGraph.nodes() }

	fun getGraphEdges() = networkLock.readLock().withLock { networkGraph.edges() }

	fun getAdjacentNodes(node: T) = networkLock.readLock().withLock { networkGraph.adjacentNodes(node) }

	fun addNode(node: T) = networkLock.writeLock().withLock {
		networkGraph.addNode(node)
		nodeMirror[node.location] = node
	}

	fun removeNode(node: T) = networkLock.writeLock().withLock {
		networkGraph.removeNode(node)
		nodeMirror.remove(node.location)
	}

	fun addEdge(nodeOne: T, nodeTwo: T, edge: GraphEdge) = networkLock.writeLock().withLock {
		networkGraph.addEdge(nodeOne, nodeTwo, edge)
	}

	abstract fun createEdge(nodeOne: T, nodeTwo: T): GraphEdge

	open fun onModified() {}
	open fun onEdgeConnected(edge: GraphEdge) {}

	fun onNewPosition(globalKey: BlockKey): Boolean {
		val block = getBlockIfLoaded(manager.transportManager.getWorld(), getX(globalKey), getY(globalKey), getZ(globalKey)) ?: return false
		val new = manager.getNode(block) ?: return false

		addPosition(new)
		return true
	}

	private fun addPosition(new: T) {
		val localPosition = toBlockKey(manager.transportManager.getLocalCoordinate(toVec3i(new.location)))

		if (nodeMirror.contains(localPosition)) return
		if (getGraphNodes().any { node -> node.location == new.location }) return

		addNode(new)

		onModified()

		val adjacentNodes = getAdjacent(new)
		for (connected in adjacentNodes) {
			val edge = createEdge(connected, new)
			addEdge(new, connected, edge)
			onEdgeConnected(edge)
		}
	}

	fun removePosition(globalPosition: BlockKey) {
		val local = manager.transportManager.getLocalCoordinate(toVec3i(globalPosition))
		val node = nodeMirror.remove(toBlockKey(local)) ?: return

		removeNode(node)
	}

	fun getAdjacent(from: T): Set<T> {
		val found = mutableSetOf<T>()

		ADJACENT_BLOCK_FACES.forEach { face ->
			nodeMirror[getRelative(from.location, face)]?.let(found::add)
		}

		return found
	}

	abstract fun tick()

	fun intakeNodes(other : TransportNodeGraph<T>) {
        // Loop all edges in other grid.
       	val newPositions = LongOpenHashSet()

		val otherEdges = other.getGraphEdges()
        for (edge: GraphEdge in otherEdges) {
            val a = edge.nodeOne as T
            val b = edge.nodeTwo as T

			newPositions.add(a.location)
			newPositions.add(b.location)

            // Insert edge and value.
			runCatching { addEdge(a, b, createEdge(a, b)) }
        }

        other.getGraphNodes().forEach { node ->  newPositions.add(node.location)}

        // Iterate all in-world nodes and update the tracked grid.
		manager.setGraphLookup(newPositions, this)

        // Insert all nodes into the Grid's lookup maps and update the node about the grid change.
        for (node in other.getGraphNodes()) {
			addNode(node)
        }
	}

	fun addNodes(nodes: Set<T>) {
		nodes.forEach(::addNode)
	}
}
