package net.horizonsend.ion.server.features.transport.cache

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.cache.state.CacheState
import net.horizonsend.ion.server.features.transport.node.manager.holders.NetworkHolder
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import org.bukkit.Material
import org.bukkit.block.BlockFace

abstract class TransportCache(val holder: NetworkHolder<*> /* TODO temp network holder, works for now */) {
	private val cache: Long2ObjectOpenHashMap<CacheState> = Long2ObjectOpenHashMap()

	fun isCached(at: BlockKey): Boolean = cache.containsKey(at)

	fun getCached(at: BlockKey): CachedNode? {
		val state = cache[at] ?: return null
		return when (state) {
			is CacheState.Empty -> null
			is CacheState.Present -> state.node
		}
	}

	fun cache(location: BlockKey, material: Material): CachedNode? {
		val type = getNodeType(material)
		val state = if (type == null) CacheState.Empty else CacheState.Present(type)

		cache[location] = state
		return type
	}

	abstract fun getNodeType(material: Material): CachedNode?

	fun getNextNodeLocations(location: BlockKey, from: BlockFace): Map<BlockFace, BlockKey> {
		val cachedAt = if (!isCached(location)) {
			// If the requested node is not cached, cache the node at the location
			val material = getBlockTypeSafe(holder.getWorld(), getX(location), getY(location), getZ(location)) ?: return mapOf()
			cache(location, material)
		} else getCached(location) // If it is already cached, this function will handle the empty node case

		if (cachedAt == null) return mapOf()

		return cachedAt.getNextNodes(from).associateWith { getRelative(location, it) }
	}


}
