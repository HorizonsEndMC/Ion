package net.horizonsend.ion.server.features.transport.manager.graph

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap

abstract class GraphManager<N : GraphNode, T: TransportNodeGraph<N>>(val transportManager: TransportManager<*>) {
	protected abstract val cacheFactory: BlockBasedCacheFactory<N, GraphManager<N, T>>

	fun getNode(block: Block): N? = cacheFactory.cache(block, this)

	private val graphs = ConcurrentHashMap.newKeySet<T>()

	private val graphLookup = Long2ObjectOpenHashMap<T>()
	private val graphPositions = Object2ObjectOpenHashMap<T, LongOpenHashSet>()

	fun removeGraph(graph: T) {
		graphs.remove(graph)
		removeGraphLookups(graph)
	}

	fun removeGraphLookups(graph: T) {
		val positions: LongOpenHashSet? = graphPositions.remove(graph)
		if (positions.isNullOrEmpty()) return

		positions.forEach(graphLookup::remove)
	}

//	fun removeGraphLookup(graph: T, position: BlockKey) {
//
//	}

	fun setGraphLookup(position: BlockKey, graph: T) {
		graphLookup[position] = graph
		graphPositions.getOrPut(graph, ::LongOpenHashSet).add(position)
	}

	fun setGraphLookup(positions: Collection<BlockKey>, graph: TransportNodeGraph<N>) {
		for (position in positions) {
			graphLookup[position] = graph as T
			graphPositions.getOrPut(graph, ::LongOpenHashSet).add(position)
		}
	}

	fun getGraphAt(location: BlockKey): T? {
		val atLocation = graphLookup[location]
		if (atLocation != null) return atLocation

		val block = getBlockIfLoaded(transportManager.getWorld(), getX(location), getY(location), getZ(location)) ?: return null
		val node = getNode(block)

		// Cannot create a graph without an origin node
		if (node == null) return null

		registerNewGraph(location)

		return graphLookup[location]
	}

	private fun registerNewGraph(location: BlockKey): Boolean {
		val new = createGraph()

		graphs.add(new)
		graphLookup[location] = new

		return new.onNewPosition(location)
	}

	abstract fun createGraph(): T

	fun cachePoint(location: BlockKey): Boolean {
		val graph = getGraphAt(location)

		if (graph != null) {
			return graph.onNewPosition(location)
		}

		val adjacentGraphs = ADJACENT_BLOCK_FACES.mapNotNullTo(mutableSetOf()) { getGraphAt(getRelative(location, it)) }

		if (adjacentGraphs.size == 1) {
			return adjacentGraphs.first().onNewPosition(location)
		}

		return combineGraphs(adjacentGraphs).onNewPosition(location)
	}

	fun combineGraphs(graphs: Iterable<T>): T {

		val sorted = graphs.sortedBy { it.getGraphNodes().size }

		val iterator = sorted.iterator()
		val mergeTraget = iterator.next()

		if (!iterator.hasNext()) return mergeTraget

		while (iterator.hasNext()) {
			val toMerge = iterator.next()

			mergeTraget.intakeNodes(toMerge);

			removeGraph(toMerge)
		}

		mergeTraget.onModified()

		return mergeTraget
	}

	fun tick() {
		graphs.forEach { t ->
			t.getGraphEdges().forEach { edge -> edge.getDisplayPoints().forEach { point ->
				debugAudience.audiences().filterIsInstance<Player>().forEach { t -> t.spawnParticle(Particle.SOUL_FIRE_FLAME, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0, null, true) }
			} }
			t.getGraphNodes().forEach { edge ->
				val point = edge.getCenter()
				debugAudience.audiences().filterIsInstance<Player>().forEach { t -> t.spawnParticle(Particle.SOUL_FIRE_FLAME, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0, null, true) }
			}
		}

		if (graphs.isEmpty()) return
		graphs.forEach { t -> t.tick() }
	}

	fun separateNode(graph: T): Boolean {
		// Generate the grid nodes isolated from each other.
		val splitGraphs: List<Set<N>> = separateGraphPositions(graph)

		if (splitGraphs.size <= 1) return false

		// Create new nodes
		splitGraphs.forEach { nodes: Set<N> ->
			createGraph().addNodes(nodes)
		}

		return true
	}

	/**
	 * Splits a multi node's positions into multiple nodes
	 * https://github.com/CoFH/ThermalDynamics/blob/1.20.x/src/main/java/cofh/thermal/dynamics/common/grid/GridContainer.java#L394
	 **/
	fun separateGraphPositions(graph: T): List<Set<N>> {
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
}

