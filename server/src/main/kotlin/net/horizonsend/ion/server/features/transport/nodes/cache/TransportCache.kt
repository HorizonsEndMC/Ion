package net.horizonsend.ion.server.features.transport.nodes.cache

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.block.Block

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
}
