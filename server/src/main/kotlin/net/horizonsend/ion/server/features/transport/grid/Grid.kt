package net.horizonsend.ion.server.features.transport.grid

import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.grid.sink.Sink
import net.horizonsend.ion.server.features.transport.grid.sink.Source
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Suppress("UnstableApiUsage")
abstract class Grid<Src: Source, Snk: Sink>(val type: GridType, val manager: WorldGridManager, val source: KClass<Src>, val sink: KClass<Snk>) {
	private val sourceList: ConcurrentHashMap.KeySetView<Src, Boolean> = ConcurrentHashMap.newKeySet()
	private val sinkList: ConcurrentHashMap.KeySetView<Snk, Boolean> = ConcurrentHashMap.newKeySet()

	val nodes: ObjectOpenHashSet<TransportNode> = ObjectOpenHashSet()

	val graph: MutableGraph<TransportNode> = GraphBuilder
		.undirected()
		.nodeOrder<TransportNode>(ElementOrder.unordered())
		.build()

	abstract fun transferResources(from: Src, to: Snk, resistanceContribution: Int, totalResistance: Int)

	fun tickTransport() {
		for (source in sourceList) {
			val sinkResistance = sinkList.associateWithNotNull { sink -> getLeastResistantPath(sink, source) }

			distributeResources(source, sinkResistance)
		}
	}

	private fun distributeResources(source: Src, sinks: Map<Snk, Int>) {
		val total = sinks.values.sum()

		for ((sink, resistance) in sinks) {
			transferResources(source, sink, resistance, total)
		}
	}

	/**
	 * Finds the path between the two sources with the least resistance, and returns the resistance value. Null if no path could be found.
	 **/
	fun getLeastResistantPath(to: Snk, from: Src): Int? {
		return 1 //TODO A*
	}

	fun addNode(node: TransportNode) {
		node.grid = this
		nodes.add(node)
		graph.addNode(node)

		if (source.isInstance(node)) {
			@Suppress("UNCHECKED_CAST")
			sourceList.add(node as Src)
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

	@Suppress("UNCHECKED_CAST")
	fun cast(grid: Grid<*, *>): Grid<Src, Snk> = grid as Grid<Src, Snk>

	fun handleMerge(other: Grid<*, *>) {
		val cast = cast(other)

		for (edge in cast.graph.edges()) {
			graph.putEdge(edge.nodeU(), edge.nodeV())
		}

		for (node in cast.graph.nodes()) {
			addNode(node)
			graph.addNode(node)
			node.grid = this
		}



		postMerge(cast)
	}

	abstract fun postMerge(other: Grid<Src, Snk>)

	abstract fun postSplit(new: List<Grid<Src, Snk>>)

	fun registerSource(source: Src) {
		sourceList.add(source)
	}

	fun removeSource(source: Source) {
		sourceList.remove(source)
	}

	fun registerSink(sink: Snk) {
		sinkList.add(sink)
	}

	fun removeSink(sink: Sink) {
		sinkList.remove(sink)
	}
}
