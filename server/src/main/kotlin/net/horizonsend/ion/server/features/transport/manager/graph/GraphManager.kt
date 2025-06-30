package net.horizonsend.ion.server.features.transport.manager.graph

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class GraphManager<N : GraphNode, T: TransportNodeGraph<N>>(val transportManager: TransportManager<*>) {
	val graphs = ObjectOpenHashSet<T>()

	val graphPositions = Long2ObjectOpenHashMap<T>()

	fun registerNewGraph(location: BlockKey) {
		val new = createGraph()

		graphs.add(new)

		graphPositions[location] = new

		new.onNewPosition(location)
	}

	fun add(location: BlockKey) {
		if (graphs.isEmpty()) {
			registerNewGraph(location)
			return
		}

		graphs.firstOrNull()?.let {
			it.onNewPosition(location)
			graphPositions[location] = it
		}
	}

	abstract fun createGraph(): T

	fun tick() {


		graphs.forEach { t -> t.tick() }
	}
}
