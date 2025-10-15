package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.filters.manager.FilterCache
import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.FilterManagedNode
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.World
import kotlin.properties.Delegates

class ShipCacheHolder<T: TransportCache>(override val transportManager: ShipTransportManager) : CacheHolder<T>(transportManager) {
	override var cache: T by Delegates.notNull(); private set

	constructor(manager: ShipTransportManager, network: (ShipCacheHolder<T>) -> T) : this(manager) {
		this.cache = network(this)
	}

	override fun getWorld(): World = transportManager.starship.world

	override fun handleLoad() = Tasks.async {
		cache.markReady()

		val possibleFilters = mutableSetOf<Vec3i>()

		transportManager.starship.iterateBlocks { x, y, z ->
			IonChunk[transportManager.starship.world, x.shr(4), z.shr(4)]?.let {
				cache.type.get(it).invalidate(x, y, z, null)
			}

			val local = transportManager.getLocalCoordinate(Vec3i(x, y, z))
			val block = getBlockIfLoaded(transportManager.starship.world, x, y, z) ?: return@async

			// Cache at local coordinate
			val cached = cache.cache(toBlockKey(local), block)
			if (cached is FilterManagedNode) {
				possibleFilters.add(local)
			}
		}

		transportManager.filterCache.loadFilters(possibleFilters)
	}

	override fun getInternalNode(key: BlockKey): Node? {
		return cache.getCached(key)
	}

	override fun getOrCacheGlobalNode(key: BlockKey): Node? {
		// Ship networks cannot access the outside world
		return getInternalNode(key)
	}

	override fun getMultiblockManager(): MultiblockManager {
		return transportManager.starship.multiblockManager
	}

	override fun getExtractorManager(): ExtractorManager {
		return transportManager.extractorManager
	}

	override fun getFilterManager(): FilterCache {
		return transportManager.filterCache
	}

	fun displace(movement: StarshipMovement) {
		cache.displace(movement)
	}

	fun release() {
		Tasks.async {
			cache.getRawCache().keys.forEach { key -> cache.getCached(key)?.onInvalidate() }
			transportManager.starship.iterateBlocks { x, y, z ->
				NewTransport.invalidateCache(getWorld(), x, y, z, null)
			}
		}
	}

	override val globalNodeLookup: CacheProvider = { _, _, pos -> cache to getInternalNode(pos) }
	override val globalNodeCacher: CacheProvider = globalNodeLookup

	override fun getInputManager(): IOManager {
		return transportManager.ioManager
	}

	override fun getCacheHolderAt(key: BlockKey): CacheHolder<T> {
		return this
	}

	override fun isLocal(key: BlockKey): Boolean {
		return transportManager.starship.blocks.contains(toVec3i(key).toBlockKey())
	}
}
