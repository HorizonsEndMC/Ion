package net.horizonsend.ion.server.features.transport.manager.graph

import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.inputs.IOPort
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidGraphEdge
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

@Suppress("UnstableApiUsage")
abstract class FlowTrackingTransportGraph<T : FlowNode, P : IOPort>(uuid: UUID, override val manager: NetworkManager<T, TransportNetwork<T>>, val ioType: IOType<P>) : TransportNetwork<T>(uuid, manager) {
	/**
	 * A map of each node location to the maximum flow achievable at that node
	 **/
	protected var flowMap = Long2DoubleOpenHashMap(); private set

	// Array of unique paths that contribute flow
	protected var paths: Array<List<BlockKey>> = arrayOf(); private set
	// Multimap of nodes to indexes of paths that use that node
	protected var nodePathLookup = Long2ObjectOpenHashMap<IntArray>(); private set

	protected var lastSinks: ObjectOpenHashSet<T> = ObjectOpenHashSet(); private set
	protected var lastSources: ObjectOpenHashSet<T> = ObjectOpenHashSet(); private set

	fun getFlow(position: Long) = flowMap.getOrDefault(position, 0.0)

	companion object {
		private const val SUPER_SOURCE = Long.MAX_VALUE
		private const val SUPER_SINK = Long.MIN_VALUE
	}

	abstract fun isSink(node: FlowNode, ioData: P): Boolean

	abstract fun isSource(node: FlowNode, ioData: P): Boolean

	open fun getFlowCapacity(node: T): Double = node.flowCapacity

	/**
	 * Runs a multi node and multi sink implementation of the Edmonds-Karp algorithm to determine flow direction and magnitude throughout the network
	 **/
	protected fun edmondsKarp() {
		// Map of nodes to all nodes that connect to them
		val parentRelationMap = Long2LongOpenHashMap()

		val sources = ObjectOpenHashSet<T>()
		val sinks = ObjectOpenHashSet<T>()

		getGraphNodes().forEach { node ->
			if (manager.transportManager.getInputProvider().getPorts(ioType, node.location).any { input -> isSink(node, input) }) sinks.add(node)

			if (manager.transportManager.getInputProvider().getPorts(ioType, node.location).any { input -> isSource(node, input) }) sources.add(node)
		}

		lastSinks = sinks
		lastSources = sources

		if (sources.isEmpty()) {
			for (node in sinks) {
				flowMap[node.location] = node.flowCapacity
			}
			return
		}

		val valueGraph = getValueGraphRepresentation()

		// Connect all sources to a super source, with a maximum capcity between
		for (source in sources) {
			valueGraph.putEdgeValue(SUPER_SOURCE, source.location, Double.MAX_VALUE)
		}

		// Connect all sinks to a super sink, with a maximum capcity between
		for (sink in sinks) {
			valueGraph.putEdgeValue(sink.location, SUPER_SINK, getFlowCapacity(sink))
		}

		var maxFlow = 0.0

		val endpointFlows = Long2DoubleOpenHashMap()

		val paths = mutableListOf<List<BlockKey>>()
		val lookup = multimapOf<BlockKey, Int>()

		var iterations = 0
		while (bfs(valueGraph, parentRelationMap))	{
			iterations++

			if (iterations > 20) {
				IonServer.slF4JLogger.warn("BFS took too long!")
				break
			}

			var pathFlow = Double.MAX_VALUE
			var node: BlockKey = SUPER_SINK

			// Set flow across path
			while (node != SUPER_SOURCE) {
				val parentOfNode: Long = parentRelationMap.getOrDefault(node, null) ?: break

				pathFlow = minOf(pathFlow, valueGraph.edgeValue(parentOfNode, node).get())
				node = parentOfNode
			}

			maxFlow += pathFlow

			// Loop over nodes and decrement the flow values from the previous loop
			var v: BlockKey = SUPER_SINK
			while (v != SUPER_SOURCE) {
				val parentOfNode: Long = parentRelationMap.getOrDefault(v, null) ?: break

				if (v == parentOfNode) break

				valueGraph.putEdgeValue(parentOfNode, v, valueGraph.edgeValue(parentOfNode, v).get() - pathFlow)
				valueGraph.putEdgeValue(v, parentOfNode, valueGraph.edgeValue(v, parentOfNode).getOrDefault(0.0) + pathFlow)

				v = parentOfNode
			}

			v = SUPER_SINK

			val path = mutableListOf<BlockKey>()
			val nextIndex = paths.size

			// Update additional info
			while (v != SUPER_SOURCE) {
				endpointFlows[v] = maxOf(endpointFlows.getOrDefault(v, 0.0), maxFlow)

				val parentOfNode: Long = parentRelationMap.getOrDefault(v, null) ?: break

				if (v == parentOfNode) break

				path.add(v)
				lookup[v].add(nextIndex)

				val parentNode = getNodeAtLocation(parentOfNode)
				val node = getNodeAtLocation(v)

				if (parentNode != null && node != null) {
					val edgeConnecting = getGraph().edgeConnecting(parentNode, node).getOrNull()

					edgeConnecting?.let { edge ->
						if (edge !is FluidGraphEdge) return@let

						val newFlow = maxOf(edge.netFlow, maxFlow)
						edge.netFlow = newFlow
					}
				}

				v = parentOfNode
			}

			paths.add(path)
		}

		flowMap = endpointFlows

		nodePathLookup = lookup.asMap().entries.associateTo(Long2ObjectOpenHashMap()) { it.key to it.value.toIntArray() }
		this.paths = paths.toTypedArray()
	}

	fun getValueGraphRepresentation(): MutableValueGraph<BlockKey, Double> {
		val copied = ValueGraphBuilder
			.directed()
			.allowsSelfLoops(false)
			.expectedNodeCount(getGraphNodes().size)
			.build<BlockKey, Double>()

		for (node in getGraphNodes()) {
			copied.addNode(node.location)
		}

		for (edge in getGraphEdges()) {
			@Suppress("UNCHECKED_CAST") val capacity = getFlowCapacity(edge.nodeOne as T)
			copied.putEdgeValue(edge.nodeOne.location, edge.nodeTwo.location, capacity)
		}

		return copied
	}

	private fun bfs(valueGraphReprestation: ValueGraph<BlockKey, Double>, parents: Long2LongOpenHashMap): Boolean {
		val visited = LongOpenHashSet()
		val queue = ArrayDeque<BlockKey>()

		queue.add(SUPER_SOURCE)
		visited.add(SUPER_SOURCE)

		var iterations = 0L

		while (queue.isNotEmpty()) {
			val parent = queue.removeFirstOrNull() ?: break

			iterations++

			for (successor in valueGraphReprestation.successors(parent)) {
				if (visited.contains(successor)) continue

				val capacity = valueGraphReprestation.edgeValue(parent, successor).getOrNull() ?: continue
				if (capacity <= 0.0) continue

				visited.add(successor)
				queue.addLast(successor)

				parents[successor] = (parent)
			}
		}

		return visited.contains(SUPER_SINK)
	}
}
