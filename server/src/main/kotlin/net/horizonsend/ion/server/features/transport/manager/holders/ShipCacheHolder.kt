package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.World
import kotlin.properties.Delegates

class ShipCacheHolder<T: TransportCache>(val manager: ShipTransportManager) : CacheHolder<T> {
	override var cache: T by Delegates.notNull(); private set

	constructor(manager: ShipTransportManager, network: (ShipCacheHolder<T>) -> T) : this(manager) {
		this.cache = network(this)
	}

	override fun getWorld(): World = manager.starship.world

	override fun handleLoad() {
		manager.starship.iterateBlocks { x, y, z ->
			IonChunk[manager.starship.world, x, z]?.let { cache.type.get(it).invalidate(x, y, z) }
			cache.cache(toBlockKey(x, y, z))
		}
	}

	override fun getInternalNode(key: BlockKey): Node? {
		return cache.getCached(key)
	}

	override fun getOrCacheGlobalNode(key: BlockKey): Node? {
		// Ship networks cannot access the outside world
		return getInternalNode(key)
	}

	override fun getMultiblockManager(): MultiblockManager {
		return manager.starship.multiblockManager
	}

	override fun getExtractorManager(): ExtractorManager {
		return manager.extractorManager
	}
}
