package net.horizonsend.ion.server.features.transport.manager.holders

import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import org.bukkit.World
import kotlin.properties.Delegates

class ChunkCacheHolder<T: TransportCache> private constructor (val manager: ChunkTransportManager) : CacheHolder<T> {
	override var cache: T by Delegates.notNull(); private set

	constructor(manager: ChunkTransportManager, network: (ChunkCacheHolder<T>) -> T) : this(manager) {
		this.cache = network(this)
	}

	override fun getWorld(): World = manager.chunk.world

	override fun getOrCacheGlobalNode(key: BlockKey): Node? {
		val chunkX = getX(key).shr(4)
		val chunkZ = getZ(key).shr(4)

		val isThisChunk = chunkX == manager.chunk.x && chunkZ == manager.chunk.z

		if (isThisChunk) {
			return cache.getOrCache(key)
		}

		val xDiff = manager.chunk.x - chunkX
		val zDiff = manager.chunk.z - chunkZ

		if (xDiff > 1 || xDiff < -1) return null
		if (zDiff > 1 || zDiff < -1) return null

		val chunk = IonChunk[getWorld(), chunkX, chunkZ] ?: return null
		return cache.type.get(chunk).getOrCache(key)
	}

	override fun getInternalNode(key: BlockKey): Node? {
		return cache.getOrCache(key)
	}

	override fun getMultiblockManager(): MultiblockManager {
		return manager.chunk.multiblockManager
	}

	override fun getExtractorManager(): ExtractorManager {
		return manager.extractorManager
	}
}
