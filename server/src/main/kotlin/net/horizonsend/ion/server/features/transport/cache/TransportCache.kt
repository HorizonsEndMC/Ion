package net.horizonsend.ion.server.features.transport.cache

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.cache.state.CacheState
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

abstract class TransportCache(val holder: NetworkHolder<*> /* TODO temp network holder, works for now */) {
	private val cache: Long2ObjectOpenHashMap<CacheState> = Long2ObjectOpenHashMap()

	abstract val type: NetworkType
	abstract val nodeFactory: NodeCacheFactory

	fun isCached(at: BlockKey): Boolean = cache.containsKey(at)

	fun getCached(at: BlockKey): CachedNode? {
		val state = cache[at] ?: return null
		return when (state) {
			is CacheState.Empty -> null
			is CacheState.Present -> state.node
		}
	}

	fun getOrCache(location: BlockKey): CachedNode? {
		if (isCached(location)) return getCached(location)
			else return cache(location, getBlockIfLoaded(holder.getWorld(), getX(location), getY(location), getZ(location)) ?: return null)
	}

	fun cache(location: BlockKey) {
		val world = holder.getWorld()
		val block = getBlockIfLoaded(world, getX(location), getY(location), getZ(location)) ?: return
		cache(location, block)
	}

	fun cache(location: BlockKey, block: Block): CachedNode? {
		val type = nodeFactory.cache(block)
		val state = if (type == null) CacheState.Empty else CacheState.Present(type)

		cache[location] = state
		return type
	}

	fun getNextNodeLocations(location: BlockKey, from: BlockFace): List<Triple<BlockFace, BlockKey, Int>> {
		val cachedAt = getOrCache(location) ?: return listOf()
		return cachedAt.getNextNodes(from).map { (face, priority) -> Triple(face, getRelative(location, face), priority) }
	}

	fun invalidate(x: Int, y: Int, z: Int) {
		return invalidate(toBlockKey(x, y, z))
	}

	fun invalidate(key: BlockKey) {

	}

	fun getRawCache() = cache
}
