package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.Long2IntRBTreeMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.path.PathCache
import net.horizonsend.ion.server.features.transport.nodes.types.ComplexNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.MAX_PATHFINDS_OVER_BLOCK
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
	private var nodeCache: ConcurrentHashMap<BlockKey, CacheState> = ConcurrentHashMap()

	/**
	 * A table containing cached paths. The first value is the origin of the path, usually an extractor, and the second is the destination location.
	 **/
	abstract val pathCache: PathCache<*>

	abstract val type: CacheType
	val nodeFactory: NodeCacheFactory get() = type.nodeCacheFactory

	abstract fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?)

	fun isCached(at: BlockKey): Boolean = nodeCache.containsKey(at)

	fun getCached(at: BlockKey): Node? {
		val state = nodeCache[at] ?: return null
		return when (state) {
			is CacheState.Empty -> null
			is CacheState.Present -> state.node
		}
	}

	fun getOrCache(location: BlockKey): Node? {
		if (isCached(location)) return getCached(location)
			else return cache(location, getBlockIfLoaded(holder.getWorld(), getX(location), getY(location), getZ(location)) ?: return null)
	}

	fun cache(location: BlockKey) {
		val world = holder.getWorld()
		val block = getBlockIfLoaded(world, getX(location), getY(location), getZ(location)) ?: return
		cache(location, block)
	}

	private val mutex = Any()

	fun cache(location: BlockKey, block: Block): Node? = synchronized(mutex)  {
		// On race conditions
		nodeCache[location]?.let { return@synchronized (it as? CacheState.Present)?.node }

		val type = nodeFactory.cache(block, this.holder)
		val state = if (type == null) CacheState.Empty else CacheState.Present(type)

		nodeCache[location] = state
		return type
	}

	fun invalidate(x: Int, y: Int, z: Int) {
		invalidate(toBlockKey(x, y, z))
	}

	abstract val extractorNodeClass: KClass<out Node>

	fun invalidate(key: BlockKey) {
		val removed = (nodeCache.remove(key) as? CacheState.Present)?.node
		removed?.onInvalidate() ?: return
		pathCache.invalidatePaths(key, removed)
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

		return registered.plus(adjacentBlocks)
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
		Vec3i(-1, 0, -1),
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

	inline fun <reified T: Node> getNetworkDestinations(
		originPos: BlockKey,
		originNode: Node,
		noinline check: ((NodePositionData) -> Boolean)? = null
	): Collection<BlockKey> = getNetworkDestinations(T::class, originPos, originNode, check)

	fun getNetworkDestinations(
		clazz: KClass<out Node>,
		originPos: BlockKey,
		originNode: Node,
		nodeCheck: ((NodePositionData) -> Boolean)? = null,
		nextNodeProvider: NodePositionData.() -> List<NodePositionData> = { getNextNodes(holder.nodeProvider, null) }
	): Collection<BlockKey> {
		val visitQueue = ArrayDeque<NodePositionData>()

		val visited = Long2IntRBTreeMap()

		fun markVisited(node: NodePositionData) {
			val pos = node.position
			val existing = visited.getOrDefault(pos, 0)

			visited[pos] = existing + 1
		}

		fun canVisit(node: NodePositionData): Boolean {
			return visited.getOrDefault(node.position, 0) < MAX_PATHFINDS_OVER_BLOCK
		}

		val destinations = LongOpenHashSet()

		visitQueue.addAll(originNode.getNextNodes(
			world = holder.getWorld(),
			position = originPos,
			backwards = BlockFace.SELF,
			holder.nodeProvider,
			null
		))

		var iterations = 0L
		val upperBound = 20_000

		while (visitQueue.isNotEmpty() && iterations < upperBound) {
			iterations++
			val current = visitQueue.removeFirst()
			markVisited(current)

			if (clazz.isInstance(current.type) && (nodeCheck?.invoke(current) != false)) {
				destinations.add(current.position)
			}

			visitQueue.addAll(nextNodeProvider(current).filterNot { !canVisit(it) || visitQueue.contains(it) })
		}

		return destinations
	}

	data class PathfindingReport(val traversedNodes: Array<NodePositionData>, val resistance: Double) {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as PathfindingReport

			if (resistance != other.resistance) return false
			if (!traversedNodes.contentEquals(other.traversedNodes)) return false

			return true
		}

		override fun hashCode(): Int {
			var result = resistance.hashCode()
			result = 31 * result + traversedNodes.contentHashCode()
			return result
		}
	}
}

// I hate this function but it works
fun getSorted(pathResistance: Array<Double?>): IntArray {
	// Store the shuffled indicies
	val ranks = IntArray(pathResistance.size) { it }
	val tempSorted = pathResistance.clone()

	for (index in ranks.indices) {
		for (j in 0..< ranks.lastIndex) {
			if ((tempSorted[j] ?: Double.MAX_VALUE) > (tempSorted[j + 1] ?: Double.MAX_VALUE)) {
				val temp = tempSorted[j]
				tempSorted[j] = tempSorted[j + 1]
				tempSorted[j + 1] = temp

				val prev = ranks[j]
				ranks[j] = prev + 1
				ranks[j + 1] = prev
			}
		}
	}

	return ranks
}
