package net.horizonsend.ion.server.features.transport.manager.graph

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import github.scarsz.discordsrv.dependencies.alexh.Fluent
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphEdge
import net.horizonsend.ion.server.features.transport.nodes.graph.GraphNode
import net.horizonsend.ion.server.features.transport.nodes.util.BlockBasedCacheFactory
import net.horizonsend.ion.server.features.transport.nodes.util.CacheState
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block
import java.util.UUID

@Suppress("UnstableApiUsage")
abstract class TransportNodeGraph<T: GraphNode>(val uuid: UUID, open val manager: GraphManager<*, *>) {
	abstract val cacheFactory: BlockBasedCacheFactory<T, TransportNodeGraph<T>>

	val nodes = Fluent.ConcurrentHashMap<BlockKey, CacheState<T>>()

	fun isCached(at: BlockKey): Boolean = nodes.containsKey(at)

	fun getCached(at: BlockKey): T? = nodes[at]?.get()

	fun getOrCache(location: BlockKey): T? {
		return if (isCached(location)) getCached(location)
		else cache(location)
	}

	fun cache(location: BlockKey): T? {
		val world = manager.transportManager.getWorld()
		val block = getBlockIfLoaded(world, getX(location), getY(location), getZ(location)) ?: return null
		return cache(location, block)
	}

	fun cache(location: BlockKey, block: Block): T? {
		return nodes.computeIfAbsent(location) { _ ->
			val type = cacheFactory.cache(block, this)
			return@computeIfAbsent if (type == null) CacheState.Empty() else CacheState.Present(type)
		}.get()
	}

	val networkGraph: MutableNetwork<T, GraphEdge> = NetworkBuilder.directed()
		.allowsParallelEdges(false)
		.allowsSelfLoops(false)
		.build()

	abstract fun getEdge(nodeOne: T, nodeTwo: T): GraphEdge

	open fun onNodeAdded(new: T) {}
	open fun onEdgeConnected(edge: GraphEdge) {}

	fun onNewPosition(globalKey: BlockKey) {
		Tasks.async {
			val block = getBlockIfLoaded(manager.transportManager.getWorld(), getX(globalKey), getY(globalKey), getZ(globalKey)) ?: return@async
			val new = cacheFactory.cache(block, this) ?: return@async

			addPosition(new)
		}
	}

	private fun addPosition(new: T) {
		val localPosition = toBlockKey(manager.transportManager.getLocalCoordinate(toVec3i(new.location)))

		if (nodes.contains(localPosition)) return

		networkGraph.addNode(new)

		nodes[localPosition] = CacheState.Present(new)

		val adjacentNodes = getAdjacent(new)
		for (connected in adjacentNodes) {
			val edge = getEdge(connected, new)
			networkGraph.addEdge(new, connected, edge)
			onEdgeConnected(edge)
		}
	}

	fun removePosition(globalPosition: BlockKey) {
		val local = manager.transportManager.getLocalCoordinate(toVec3i(globalPosition))
		val node = nodes.remove(toBlockKey(local))?.get() ?: return

		networkGraph.removeNode(node)
	}

	fun getAdjacent(from: T): Set<T> {
		val found = mutableSetOf<T>()

		ADJACENT_BLOCK_FACES.forEach { face ->
			nodes[getRelative(from.location, face)]?.get()?.let(found::add)
		}

		return found
	}

	abstract fun tick()
}
