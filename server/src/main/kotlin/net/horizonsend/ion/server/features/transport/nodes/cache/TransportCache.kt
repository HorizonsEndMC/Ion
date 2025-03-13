package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.util.DestinationCache
import net.horizonsend.ion.server.features.transport.nodes.pathfinding.PathfindingNodeWrapper
import net.horizonsend.ion.server.features.transport.nodes.types.ComplexNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.isAdjacent
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

abstract class TransportCache(open val holder: CacheHolder<*>) {
	/**
	 * Cache containing a cache state at their corresponding block position.
	 * The state can either be empty, or present. Empty key / value pairs have not been cached.
	 **/
	private val nodeCache: ConcurrentHashMap<BlockKey, CacheState> = ConcurrentHashMap(16, 0.5f, 64)

	@Suppress("LeakingThis")
	val destinationCache = DestinationCache(this)

	abstract val type: CacheType
	private val nodeFactory: NodeCacheFactory get() = type.nodeCacheFactory

	abstract fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?)

	private fun isCached(at: BlockKey): Boolean = nodeCache.containsKey(at)

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

	fun cache(location: BlockKey, block: Block): Node? = nodeCache.computeIfAbsent(location) { _ ->
		val type = nodeFactory.cache(block, this.holder)
		return@computeIfAbsent if (type == null) CacheState.Empty else CacheState.Present(type)
	}.get()

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

		destinationCache.invalidatePaths(key, removed)
	}

	fun invalidateSurroundingPaths(key: BlockKey) {
		ADJACENT_BLOCK_FACES.forEach {
			val relative = getRelative(key, it)
			val node = getCached(relative) ?: return@forEach
			destinationCache.invalidatePaths(relative, node)
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
		val inputManager = holder.getInputManager()
		val registered = inputManager.getHolders(type, location)

		// The stupid offsets are a list of locations that a multiblock entity would be accessible from if its sign were touching the provided location
		// Doing a call to try to find a sign is a lot more expensive since it has a getChunk call
		//
		// If this actually finds an entity, it makes sure that its sign block is adjacent to the input
		val multiblockManager = holder.getMultiblockManager()
		val adjacentBlocks = stupidOffsets.mapNotNull {
			val loc = Vec3i(it.x, it.y, it.z) + toVec3i(location)

			multiblockManager.getGlobalMultiblockEntity(holder.getWorld(), loc.x, loc.y, loc.z)?.takeIf { entity ->
				val signLoc = entity.getSignKey()
				isAdjacent(signLoc, location)
			}
		}

		registered.addAll(adjacentBlocks)

		return registered
	}

	private val stupidOffsets: Array<Vec3i> = arrayOf(
		// Upper ring
		Vec3i(1, 1, 0),
		Vec3i(-1, 1, 0),
		Vec3i(0, 1, 1),
		Vec3i(0, 1, -1),
		// Lower ring
		Vec3i(1, -1, 0),
		Vec3i(-1, -1, 0),
		Vec3i(0, -1, 1),
		Vec3i(0, -1, -1),

		// Middle ring
		Vec3i(2, 0, 0),
		Vec3i(-2, 0, 0),
		Vec3i(0, 0, -2),
		Vec3i(0, 0, -2),

		Vec3i(1, 0, 1),
		Vec3i(-1, 0, 1),
		Vec3i(1, 0, -1),
		Vec3i(-1, 0, -1)
	)

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

	inline fun <reified T: Node> getOrCacheDestination(
		originPos: BlockKey,
		originNode: Node,
		noinline check: ((NodePositionData) -> Boolean)? = null
	): Collection<PathfindingNodeWrapper> {
		val clazz = T::class
		val cachedEntry = destinationCache.get(clazz, originPos)
		if (cachedEntry != null) return cachedEntry

		val destinations = getNetworkDestinations(T::class, originPos, originNode, check)

		destinationCache.set(clazz, originPos, destinations)
		return destinations
	}

	inline fun <reified T: Node> getNetworkDestinations(
		originPos: BlockKey,
		originNode: Node,
		noinline check: ((NodePositionData) -> Boolean)? = null
	): Set<PathfindingNodeWrapper> = getNetworkDestinations(T::class, originPos, originNode, check)

	/**
	 * This is a weird combination of A* and a flood fill. It keeps track of paths, and returned destinations have those available.
	 **/
	fun getNetworkDestinations(
		clazz: KClass<out Node>,
		originPos: BlockKey,
		originNode: Node,
		nodeCheck: ((NodePositionData) -> Boolean)? = null,
		nextNodeProvider: NodePositionData.() -> List<NodePositionData> = { getNextNodes(holder.nodeCacherGetter, null) }
	): Set<PathfindingNodeWrapper> {
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
		val destinations = ObjectOpenHashSet<PathfindingNodeWrapper>()

		// Populate array with original nodes
		computeNextNodes(
			current = PathfindingNodeWrapper(
				node = NodePositionData(
					originNode,
					holder.getWorld(),
					originPos,
					BlockFace.SELF,
					this
				),
				parent = null,
				0,
				0
			),
			nextNodeProvider = nextNodeProvider,
			visitQueue = visitQueue,
			visited = visited
		)

		// So this doesn't go forever
		var iterations = 0L
		val upperBound = 20_000

		// Flood fill algorithm
		while (visitQueue.isNotEmpty() && iterations < upperBound) {
			iterations++

			// Pop the head of the queue
			val (key, current) = visitQueue.firstEntry()
			visitQueue.remove(key)

			markVisited(current.node)

			// If matches destinations, mark as such
			if (clazz.isInstance(current.node.type) && (nodeCheck?.invoke(current.node) != false)) {
				destinations.add(current)
			}

			// Populate the visit queue
			computeNextNodes(current, nextNodeProvider, visitQueue, visited)
		}

		return destinations
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

			val wrapped = PathfindingNodeWrapper(
				node = next,
				parent = current,
				g = current.g + 1,
				f = 1
			)

			if (visitQueue.contains(next.position)) {
				val existingNeighbor = visitQueue[next.position]

				if (wrapped.g < existingNeighbor.g) {
					existingNeighbor.parent = wrapped.parent

					existingNeighbor.g = wrapped.g
					existingNeighbor.f = wrapped.f
				}
			} else {
				visitQueue.put(next.position, wrapped)
			}
		}
	}
}
