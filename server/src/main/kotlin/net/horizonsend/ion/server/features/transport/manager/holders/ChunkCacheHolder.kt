package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getOrCacheNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import org.bukkit.World
import kotlin.properties.Delegates

class ChunkCacheHolder<T: TransportCache> private constructor (override val transportManager: ChunkTransportManager) : CacheHolder<T> {
	override var cache: T by Delegates.notNull(); private set

	constructor(manager: ChunkTransportManager, network: (ChunkCacheHolder<T>) -> T) : this(manager) {
		this.cache = network(this)
	}

	override fun getWorld(): World = transportManager.chunk.world

	override fun getOrCacheGlobalNode(key: BlockKey): Node? {
		val chunkX = getX(key).shr(4)
		val chunkZ = getZ(key).shr(4)

		val isThisChunk = chunkX == transportManager.chunk.x && chunkZ == transportManager.chunk.z

		if (isThisChunk) {
			return cache.getOrCache(key)
		}

		val xDiff = transportManager.chunk.x - chunkX
		val zDiff = transportManager.chunk.z - chunkZ

		// Only allow access to adjacent chunks
		if (xDiff > 1 || xDiff < -1) return null
		if (zDiff > 1 || zDiff < -1) return null

		val chunk = IonChunk[getWorld(), chunkX, chunkZ] ?: return null
		return cache.type.get(chunk).getOrCache(key)
	}

	override fun getInternalNode(key: BlockKey): Node? {
		return cache.getOrCache(key)
	}

	override fun getMultiblockManager(): MultiblockManager {
		return transportManager.chunk.multiblockManager
	}

	override fun getExtractorManager(): ExtractorManager {
		return transportManager.extractorManager
	}

	override val nodeProvider: (CacheType, World, BlockKey) -> Node? = { cacheType, world, pos ->
		getOrCacheNode(cacheType, world, pos)
	}

	override fun getInputManager(): InputManager {
		return transportManager.getInputProvider()
	}
}
