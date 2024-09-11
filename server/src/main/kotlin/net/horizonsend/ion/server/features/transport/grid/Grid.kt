package net.horizonsend.ion.server.features.transport.grid

import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.util.Sink
import net.horizonsend.ion.server.features.transport.grid.util.Source
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import java.util.concurrent.ConcurrentHashMap

@Suppress("UnstableApiUsage")
abstract class Grid(val type: GridType, val manager: WorldGridManager) {
	private val sourceList: ConcurrentHashMap.KeySetView<Source, Boolean> = ConcurrentHashMap.newKeySet()
	private val sinkList: ConcurrentHashMap.KeySetView<Sink, Boolean> = ConcurrentHashMap.newKeySet()

	val nodes: ObjectOpenHashSet<TransportNode> = ObjectOpenHashSet()

	val graph: MutableGraph<TransportNode> = GraphBuilder
		.undirected()
		.nodeOrder<TransportNode>(ElementOrder.unordered())
		.build()

	abstract fun transferResources(from: Source, to: Sink, resistanceContribution: Int, totalResistance: Int)

	fun tickTransport() {
		for (source in sourceList) {
			val sinkResistance = sinkList.associateWithNotNull { sink -> getLeastResistantPath(sink, source) }

			distributeResources(source, sinkResistance)
		}
	}

	private fun distributeResources(source: Source, sinks: Map<Sink, Int>) {
		val total = sinks.values.sum()

		for ((sink, resistance) in sinks) {
			transferResources(source, sink, resistance, total)
		}
	}

	/**
	 * Finds the path between the two sources with the least resistance, and returns the resistance value. Null if no path could be found.
	 **/
	fun getLeastResistantPath(to: Sink, from: Source): Int? {
		return 1 //TODO A*
	}

	fun addNode(node: TransportNode) {
		node.grid = this
		nodes.add(node)
		graph.addNode(node)

		if (node is Source) {
			sourceList.add(node)
		}
	}

	fun removeNode(node: TransportNode) {
		nodes.remove(node)
		graph.removeNode(node)

		if (nodes.isEmpty()) {
			manager.removeGrid(this)
		}

		if (node is Source) {
			sourceList.remove(node)
		}
	}

	fun handleMerge(other: Grid) {
		for (edge in other.graph.edges()) {
			graph.putEdge(edge.nodeU(), edge.nodeV())
		}

		for (node in other.graph.nodes()) {
			addNode(node)
			graph.addNode(node)
			node.grid = this
		}

		postMerge(other)
	}

	abstract fun postMerge(other: Grid)

	abstract fun postSplit(new: List<Grid>)

	fun registerSource(source: Source) {
		sourceList.add(source)
	}

	fun removeSource(source: Source) {
		sourceList.remove(source)
	}

	fun registerSink(sink: Sink) {
		sinkList.add(sink)
	}

	fun removeSink(sink: Sink) {
		sinkList.remove(sink)
	}
}
