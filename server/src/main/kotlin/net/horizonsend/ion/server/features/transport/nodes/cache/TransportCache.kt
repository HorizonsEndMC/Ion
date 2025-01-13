package net.horizonsend.ion.server.features.transport.nodes.cache

import com.google.common.collect.TreeBasedTable
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.ComplexNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.calculatePathResistance
import net.horizonsend.ion.server.features.transport.util.getIdealPath
import net.horizonsend.ion.server.features.transport.util.getOrCacheNode
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.isAdjacent
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.set
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

abstract class TransportCache(val holder: CacheHolder<*>) {
	private var cache: ConcurrentHashMap<BlockKey, CacheState> = ConcurrentHashMap()

	abstract val type: CacheType
	abstract val nodeFactory: NodeCacheFactory

	abstract fun tickExtractor(location: BlockKey, delta: Double)

	fun isCached(at: BlockKey): Boolean = cache.containsKey(at)

	fun getCached(at: BlockKey): Node? {
		val state = cache[at] ?: return null
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
		cache[location]?.let { return@synchronized (it as? CacheState.Present)?.node }

		val type = nodeFactory.cache(block)
		val state = if (type == null) CacheState.Empty else CacheState.Present(type)

		cache[location] = state
		return type
	}

	fun invalidate(x: Int, y: Int, z: Int) {
		invalidate(toBlockKey(x, y, z))
	}

	fun invalidate(key: BlockKey) {
		(cache.remove(key) as? CacheState.Present)?.node?.onInvalidate()
	}

	fun getRawCache() = cache

	fun displace(movement: StarshipMovement) {
		val new = ConcurrentHashMap<BlockKey, CacheState>()

		for ((key, cached) in cache) {
			val newKey = movement.displaceKey(key)
			val presentNode = (cached as? CacheState.Present)?.node
			if (presentNode is ComplexNode) presentNode.displace(movement)
			new[newKey] = cached
		}

		cache = new
	}

	/**
	 * Gets the powered entities accessible from this location, assuming it is an input
	 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
	 **/
	fun getInputEntities(location: BlockKey): Set<MultiblockEntity> {
		val inputManager = holder.getWorld().ion.inputManager
		val registered = inputManager.getHolders(type, location)

		// The stupid offsets are a list of locations that a multiblock entity would be accessible from if its sign were touching the provided location
		// Doing a call to try to find a sign is a lot more expensive since it has a getChunk call
		//
		// If this actually finds an entity, it makes sure that its sign block is adjacent to the input
		val adjacentBlocks = stupidOffsets.mapNotNull {
			MultiblockEntities.getMultiblockEntity(holder.getWorld(), it.x, it.y, it.z)?.takeIf { entity ->
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

	inline fun <reified T> getExtractorSources(extractorLocation: BlockKey, filterNot: (T) -> Boolean): List<T> {
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
		check: (Node.NodePositionData) -> Boolean,
	): List<BlockKey> {
		val originNode = getOrCacheNode(type, holder.getWorld(), originPos) ?: return listOf()

		val visitQueue = ArrayDeque<Node.NodePositionData>()
		val visited = LongOpenHashSet()
		val destinations = LongOpenHashSet()

		visitQueue.addAll(originNode.getNextNodes(
			world = holder.getWorld(),
			position = originPos,
			backwards = BlockFace.SELF,
			holder.nodeProvider,
			null
		))

		while (visitQueue.isNotEmpty()) {
			val current = visitQueue.removeFirst()
			visited.add(current.position)

			if (current.type is T && check(current)) destinations.add(current.position)


			visitQueue.addAll(current.getNextNodes(holder.nodeProvider, null).filterNot { visited.contains(it.position) || visitQueue.contains(it) })
		}

		return destinations.toList()
	}

	/**
	 *
	 **/
	private val simplePathCache = TreeBasedTable.create<BlockKey, BlockKey, Optional<PathfindingReport>>()

	fun getOrCachePath(origin: Node.NodePositionData, destination: BlockKey, pathfindingFilter: ((Node, BlockFace) -> Boolean)? = null): PathfindingReport? {
		if (simplePathCache.contains(origin.position, destination)) {
			return simplePathCache.get(origin.position, destination)?.getOrNull()
		}

		val path = runCatching { getIdealPath(origin, destination, holder.nodeProvider, pathfindingFilter) }.getOrNull()

		if (path == null) {
			simplePathCache[origin.position, destination] = Optional.empty()
			return null
		}

		val resistance = calculatePathResistance(path)

		val report = PathfindingReport(path, resistance)

		simplePathCache[origin.position, destination] = Optional.of(report)
		return report
	}

	data class PathfindingReport(val traversedNodes: Array<Node.NodePositionData>, val resistance: Double) {
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
