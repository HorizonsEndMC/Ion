package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.manager.holders.NetworkHolder
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block

abstract class TransportCache(val holder: NetworkHolder<*> /* TODO temp network holder, works for now */) {
	private val cache: Long2ObjectOpenHashMap<CacheState> = Long2ObjectOpenHashMap()

	abstract val type: NetworkType
	abstract val nodeFactory: NodeCacheFactory

	fun tick() {
		for (extractor in holder.getExtractorManager().getExtractors()) {
			tickExtractor(extractor)
		}
	}

	abstract fun tickExtractor(location: BlockKey)

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

	fun invalidate(x: Int, y: Int, z: Int) {
		invalidate(toBlockKey(x, y, z))
	}

	fun invalidate(key: BlockKey) {
		(cache.remove(key) as? CacheState.Present)?.node?.onInvalidate()
	}

	fun getRawCache() = cache
}
