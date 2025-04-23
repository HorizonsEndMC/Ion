package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.TransportTask
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.types.ComplexNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.util.CacheState
import net.horizonsend.ion.server.features.transport.nodes.util.NodeCacheFactory
import net.horizonsend.ion.server.features.transport.nodes.util.PathfindingNodeWrapper
import net.horizonsend.ion.server.features.transport.util.CacheType
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
import net.kyori.adventure.audience.Audience
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KClass

abstract class TransportCache(open val holder: CacheHolder<*>) {
	var ready: Boolean = false

	fun markReady() { ready = true }

	/**
	 * Cache containing a cache state at their corresponding block position.
	 * The state can either be empty, or present. Empty key / value pairs have not been cached.
	 **/
	private val nodeCache: ConcurrentHashMap<BlockKey, CacheState> = ConcurrentHashMap(16, 0.5f, 16)

	abstract val type: CacheType
	private val nodeFactory: NodeCacheFactory get() = type.nodeCacheFactory

	abstract fun tickExtractor(
		location: BlockKey,
		delta: Double,
		metaData: ExtractorMetaData?,
		index: Int,
		count: Int,
	)

	fun isCached(at: BlockKey): Boolean = nodeCache.containsKey(at)

	fun getCached(at: BlockKey): Node? = nodeCache[at]?.get()

	fun getOrCache(location: BlockKey): Node? {
		return if (isCached(location)) getCached(location)
		else cache(location)
	}

	fun cache(location: BlockKey): Node? {
		val world = holder.getWorld()
		val block = getBlockIfLoaded(world, getX(location), getY(location), getZ(location)) ?: return null
		return cache(location, block)
	}

	fun cache(location: BlockKey, block: Block): Node? {
		if (!ready) return null

		return nodeCache.computeIfAbsent(location) { _ ->
			val type = nodeFactory.cache(block, this.holder)
			return@computeIfAbsent if (type == null) CacheState.Empty else CacheState.Present(type)
		}.get()
	}

	fun invalidate(x: Int, y: Int, z: Int) {
		invalidate(toBlockKey(x, y, z))
	}

	abstract val extractorNodeClass: KClass<out Node>

	fun invalidate(key: BlockKey) {
		val removed = (nodeCache.remove(key) as? CacheState.Present)?.node
		removed?.onInvalidate()

		if (removed == null) {
			invalidateSurroundingPaths(key)
			return
		}

		if (this is DestinationCacheHolder) destinationCache.invalidatePaths(key, removed)
	}

	fun invalidateSurroundingPaths(key: BlockKey) {
		ADJACENT_BLOCK_FACES.forEach {
			val relative = getRelative(key, it)
			val node = getCached(relative) ?: return@forEach

			if (this is DestinationCacheHolder) destinationCache.invalidatePaths(relative, node)
		}
	}

	fun getRawCache() = nodeCache

	fun displace(movement: StarshipMovement) {
		for (state in getRawCache().values) {
			if (state !is CacheState.Present) continue
			val node = state.node
			if (node !is ComplexNode) continue

			node.displace(movement)
		}
	}

	/**
	 * Gets the powered entities accessible from this location, assuming it is an input
	 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
	 **/
	fun getInputEntities(location: BlockKey): Set<MultiblockEntity> {
		return holder.getInputManager().getHolders(type, location)
	}

	/**
	 * Gets the powered entities accessible from this location, assuming it is an input
	 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
	 **/
	inline fun <reified T> getInputEntitiesTyped(location: BlockKey): Set<T> {
		return holder.getInputManager().getHolders(type, location).filterIsInstanceTo(mutableSetOf())
	}

	inline fun <reified T> getExtractorSourceEntities(extractorLocation: BlockKey, filterNot: (T) -> Boolean): List<T> {
		val sources = mutableListOf<T>()

		for (face in ADJACENT_BLOCK_FACES) {
			val inputLocation = getRelative(extractorLocation, face)
			if (holder.getOrCacheGlobalNode(inputLocation) !is PowerNode.PowerInputNode) continue
			val entities = getInputEntities(inputLocation)

			for (entity in entities) {
				if (entity !is T) continue
				if (filterNot.invoke(entity)) continue
				sources.add(entity)
			}
		}

		return sources
	}

	inline fun <reified T: Node> getOrCacheNetworkDestinations(
		task: TransportTask,
		originPos: BlockKey,
		originNode: Node,
		retainFullPath: Boolean,

		cacheGetter: Supplier<Array<PathfindResult>?>,
		cachingFunction: Consumer<Array<PathfindResult>>,

		noinline pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null,
		noinline destinationCheck: ((NodePositionData) -> Boolean)? = null,
		noinline nextNodeProvider: NodePositionData.() -> List<NodePositionData> = { getNextNodes(holder.globalNodeCacher, pathfindingFilter) }
	): Array<PathfindResult> {
		val cachedEntry = cacheGetter.get()
		if (cachedEntry != null) return cachedEntry

		val destinations = getNetworkDestinations(task, T::class, originPos, originNode, retainFullPath, destinationCheck, pathfindingFilter, null, nextNodeProvider)
		cachingFunction.accept(destinations)

		return destinations
	}

	inline fun <reified T: Node> getNetworkDestinations(
		task: TransportTask,
		originPos: BlockKey,
		originNode: Node,
		retainFullPath: Boolean,
		noinline pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null,
		noinline destinationCheck: ((NodePositionData) -> Boolean)? = null,
		noinline nextNodeProvider: NodePositionData.() -> List<NodePositionData> = { getNextNodes(holder.globalNodeCacher, pathfindingFilter) }
	): Array<PathfindResult> = getNetworkDestinations(task, T::class, originPos, originNode, retainFullPath, destinationCheck, pathfindingFilter, null, nextNodeProvider)

	/**
	 * This is a weird combination of A* and a flood fill. It keeps track of paths, and returned destinations have those available.
	 **/
	fun getNetworkDestinations(
		task: TransportTask,
		destinationTypeClass: KClass<out Node>,
		originPos: BlockKey,
		originNode: Node,
		retainFullPath: Boolean,
		destinationCheck: ((NodePositionData) -> Boolean)? = null,
		pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null,
		debug: Audience? = null,
		nextNodeProvider: NodePositionData.() -> List<NodePositionData> = { getNextNodes(holder.globalNodeCacher, pathfindingFilter) }
	): Array<PathfindResult> {
		val visitQueue = Long2ObjectRBTreeMap<PathfindingNodeWrapper>()
		val visited = Long2IntOpenHashMap()

		// Helper function for marking visited. Since some nodes need to be able to be traversed more than once,
		// it stores a map of nodes to number of times visited.
		fun markVisited(node: NodePositionData) {
			val pos = node.position

			var new = false
			val existing = visited.getOrPut(pos) {
				new = true
				1
			}

			if (new) return

			visited[pos] = existing + 1
		}

		// Located destinations
		val destinations = ObjectOpenHashSet<PathfindResult>()

		// Populate array with original nodes
		computeNextNodes(
			current = PathfindingNodeWrapper.newPath(NodePositionData(
					originNode,
					holder.getWorld(),
					originPos,
					BlockFace.SELF,
					this
			)),
			nextNodeProvider = nextNodeProvider,
			visitQueue = visitQueue,
			visited = visited
		)

		// So this doesn't go forever
		var iterations = 0L
		val upperBound = 20_000

		// Flood fill algorithm
		while (visitQueue.isNotEmpty() && iterations < upperBound) {
			if (task.isInterrupted()) return arrayOf()
			iterations++

			// Pop the head of the queue
			val (key, current) = visitQueue.firstEntry()
			visitQueue.remove(key)

			if (debug != null) {
				Tasks.asyncDelay(iterations) {
					debug.highlightBlock(toVec3i(current.node.position), 10L)
				}
			}

			markVisited(current.node)

			// If matches destinations, mark as such
			if (destinationTypeClass.isInstance(current.node.type) && (destinationCheck?.invoke(current.node) != false)) {
				destinations.add(PathfindResult(
					current.node.position,
					current.buildPath(retainFullPath)
				))
			}

			// Populate the visit queue
			computeNextNodes(current, nextNodeProvider, visitQueue, visited)
		}

		return destinations.toTypedArray()
	}

	private fun computeNextNodes(
		current: PathfindingNodeWrapper,
		nextNodeProvider: NodePositionData.() -> List<NodePositionData>,
		visitQueue: Long2ObjectRBTreeMap<PathfindingNodeWrapper>,
		visited: Long2IntOpenHashMap,
	) {
		fun canVisit(node: NodePositionData): Boolean {
			return visited.get(node.position) < node.type.getMaxPathfinds()
		}

		val nextNodes = nextNodeProvider(current.node)

		for (next in nextNodes) {
			if (!canVisit(next)) continue

			val wrapped = PathfindingNodeWrapper.fromParent(
				node = next,
				parent = current
			)

			if (visitQueue.contains(next.position)) {
				continue
			} else {
				visitQueue.put(next.position, wrapped)
			}
		}
	}
}
