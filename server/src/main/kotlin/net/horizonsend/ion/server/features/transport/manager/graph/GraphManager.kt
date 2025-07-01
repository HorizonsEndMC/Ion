package net.horizonsend.ion.server.features.transport.manager.graph

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
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
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

	private val graphLookup = ConcurrentHashMap<BlockKey, TransportNodeGraph<N>>()
	private val graphPositions = Object2ObjectOpenHashMap<TransportNodeGraph<N>, LongOpenHashSet>()

	fun removeGraph(graph: TransportNodeGraph<N>) {
		graphs.remove(graph)
		removeGraphLookups(graph)
	}

	fun removeGraphLookups(graph: TransportNodeGraph<N>) {
		val positions: LongOpenHashSet? = graphPositions.remove(graph)
		if (positions.isNullOrEmpty()) return

		positions.forEach(graphLookup::remove)
	}

	fun removeGraphLookup(position: BlockKey) {
		val graph = graphLookup.remove(position) ?: return
		graphPositions.getOrPut(graph, ::LongOpenHashSet).remove(position)
	}

	fun setGraphLookup(position: BlockKey, graph: TransportNodeGraph<N>) {
		graphLookup[position] = graph
		graphPositions.getOrPut(graph, ::LongOpenHashSet).add(position)
	}

	fun setGraphLookups(positions: Collection<BlockKey>, graph: TransportNodeGraph<N>) {
		for (position in positions) {
			graphLookup[position] = graph
			graphPositions.getOrPut(graph, ::LongOpenHashSet).add(position)
		}
	}

	fun getGraphAt(location: BlockKey): T? {
		val locationVec3i = toVec3i(location)
		val atLocation = cast(graphLookup[location])
		if (atLocation != null) return atLocation

		val block = getBlockIfLoaded(transportManager.getWorld(), getX(location), getY(location), getZ(location)) ?: return null
		val node = getNode(block)

		// Cannot create a graph without an origin node
		if (node == null) return null

		val adjacentGraphs = ADJACENT_BLOCK_FACES.mapNotNullTo(mutableSetOf()) { cast(graphLookup[getRelative(location, it)]) }

		if (adjacentGraphs.isNotEmpty()) {
			return combineGraphs(adjacentGraphs)
		}

		registerNewGraph(location)
		return cast(graphLookup[location])
	}

	private fun registerNewGraph(location: BlockKey): Boolean {
		val new = createGraph()

		graphs.add(new)
		setGraphLookup(location, new)

		return new.onNewPosition(location)
	}

	abstract fun createGraph(): T

	fun cachePoint(location: BlockKey): Boolean {
		return getGraphAt(location)?.onNewPosition(location) == true
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

	@Suppress("UNCHECKED_CAST")
	fun cast(graph: TransportNodeGraph<N>?): T? = graph as T?
}

