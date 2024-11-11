package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.cache.CachedNode
import net.horizonsend.ion.server.features.transport.cache.TransportCache
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.World
import kotlin.properties.Delegates

class ShipNetworkHolder<T: TransportCache>(val manager: ShipTransportManager) : NetworkHolder<T> {
	override var network: T by Delegates.notNull(); private set

	constructor(manager: ShipTransportManager, network: (ShipNetworkHolder<T>) -> T) : this(manager) {
		this.network = network(this)
	}

	override fun getWorld(): World = manager.starship.world

	override fun handleLoad() {
		manager.starship.iterateBlocks { x, y, z ->
			IonChunk[manager.starship.world, x, z]?.let { network.type.get(it).invalidate(x, y, z) }
			network.cache(toBlockKey(x, y, z))
		}
	}

	override fun getInternalNode(key: BlockKey): CachedNode? {
		return network.getCached(key)
	}

	override fun getOrCacheGlobalNode(key: BlockKey): CachedNode? {
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
