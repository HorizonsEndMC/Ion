package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.ShipTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import org.bukkit.World
import kotlin.properties.Delegates

class ShipCacheHolder<T: TransportCache>(override val transportManager: ShipTransportManager) : CacheHolder<T> {
	override var cache: T by Delegates.notNull(); private set

	constructor(manager: ShipTransportManager, network: (ShipCacheHolder<T>) -> T) : this(manager) {
		this.cache = network(this)
	}

	override fun getWorld(): World = transportManager.starship.world

	override fun handleLoad() {
		transportManager.starship.iterateBlocks { x, y, z ->
			IonChunk[transportManager.starship.world, x.shr(4), z.shr(4)]?.let { cache.type.get(it).invalidate(x, y, z) }

			val local = transportManager.getLocalCoordinate(Vec3i(x, y, z))
			val block = getBlockIfLoaded(transportManager.starship.world, x, y, z) ?: return

			// Cache at local coordinate
			cache.cache(toBlockKey(local), block)
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
		return transportManager.starship.multiblockManager
	}

	override fun getExtractorManager(): ExtractorManager {
		return transportManager.extractorManager
	}

	fun displace(movement: StarshipMovement) {
		cache.displace(movement)
	}

	fun release() {
		cache.getRawCache().keys.forEach { key -> cache.getCached(key)?.onInvalidate() }
		transportManager.starship.iterateBlocks { x, y, z ->
			NewTransport.invalidateCache(getWorld(), x, y, z)
		}
	}

	override val nodeProvider: (CacheType, World, BlockKey) -> Node? = { _, _, pos ->
		getInternalNode(pos)
	}

	override fun getInputManager(): InputManager {
		return transportManager.inputManager
	}

	override fun getCacheHolderAt(key: BlockKey): CacheHolder<T> {
		return this
	}
}
