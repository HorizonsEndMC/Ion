package net.horizonsend.ion.server.features.transport.manager.graph

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode

abstract class GraphManager<N : GraphNode, T: TransportNodeGraph<N>>(val transportManager: TransportManager<*>) {
	val graphs = ObjectOpenHashSet<T>()

	val graphPositions = Long2ObjectOpenHashMap<T>()

	fun registerNewGraph(node: N) {
		val new = createGraph()

		graphs.add(new)

		new.addPosition(node)
	}

	fun add(node: N) {
		if (graphs.isEmpty()) {
			registerNewGraph(node)
			return
		}

		graphs.firstOrNull()?.addPosition(node)
	}

	abstract fun createGraph(): T
}
