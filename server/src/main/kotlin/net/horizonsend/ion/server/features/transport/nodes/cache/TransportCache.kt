package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getOrCacheNode
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

abstract class TransportCache(val holder: CacheHolder<*>) {
	private val cache: Long2ObjectOpenHashMap<CacheState> = Long2ObjectOpenHashMap()
	private val mutex = Any()

	abstract val type: CacheType
	abstract val nodeFactory: NodeCacheFactory

	abstract fun tickExtractor(location: BlockKey, delta: Double)

	fun isCached(at: BlockKey): Boolean = synchronized(mutex) { cache.containsKey(at) }

	fun getCached(at: BlockKey): Node? = synchronized(mutex) {
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

	fun cache(location: BlockKey, block: Block): Node? = synchronized(mutex) {
		val type = nodeFactory.cache(block)
		val state = if (type == null) CacheState.Empty else CacheState.Present(type)

		cache[location] = state
		return type
	}

	fun invalidate(x: Int, y: Int, z: Int) = synchronized(mutex) {
		invalidate(toBlockKey(x, y, z))
	}

	fun invalidate(key: BlockKey) = synchronized(mutex) {
		(cache.remove(key) as? CacheState.Present)?.node?.onInvalidate()
	}

	fun getRawCache() = cache

	/**
	 * Gets the powered entities accessible from this location, assuming it is an input
	 * This method is used in conjunction with input registration to allow direct access via signs, and remote access via registered inputs
	 **/
	fun getInputEntities(location: BlockKey): Set<MultiblockEntity> {
		val inputManager = holder.getWorld().ion.inputManager
		val registered = inputManager.getHolders(type, location)

		val adjacentBlocks = stupidOffsets.mapNotNull { MultiblockEntities.getMultiblockEntity(holder.getWorld(), it.x, it.y, it.z) }

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
			null
		))

		while (visitQueue.isNotEmpty()) {
			val current = visitQueue.removeFirst()
			visited.add(current.position)

			if (current.type is T && check(current)) destinations.add(current.position)

			visitQueue.addAll(current.getNextNodes(null).filterNot { visited.contains(it.position) || visitQueue.contains(it) })
		}

		return destinations.toList()
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
