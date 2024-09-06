package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KClass

/** Contains multiblocks that need to be stored at a world level, such as base shields, mob defenders, etc. */
class WorldMultiblockManager(val world: IonWorld) {
	private val worldMultiblocks: ConcurrentHashMap<KClass<out MultiblockEntity>, LinkedBlockingQueue<MultiblockEntity>> = ConcurrentHashMap()

	fun register(multiblockEntity: MultiblockEntity): Boolean {
		val kClass = multiblockEntity::class
		return getCollection(kClass).add(multiblockEntity)
	}

	fun deregister(multiblockEntity: MultiblockEntity): Boolean {
		val kClass = multiblockEntity::class
		val success =  getCollection(kClass).remove(multiblockEntity)

		if (!success) IonServer.slF4JLogger.warn("Tried to deregister multiblock entity that was not registered!")
		return success
	}

	private fun <T : MultiblockEntity> getCollection(type: KClass<T>): LinkedBlockingQueue<MultiblockEntity> {
		return worldMultiblocks.getOrPut(type) { LinkedBlockingQueue<MultiblockEntity>() }
	}

	operator fun <T : MultiblockEntity> get(type: KClass<T>): Collection<T> {
		@Suppress("UNCHECKED_CAST")
		return getCollection(type) as Collection<T>
	}

	fun getStoredMultiblocks(): Set<KClass<out MultiblockEntity>> {
		return worldMultiblocks.keys
	}

	fun getMultiblockEntity(x: Int, y: Int, z: Int): MultiblockEntity? {
		val chunk = world.getChunk(x.shr(4), z.shr(4)) ?: return null
		val key = toBlockKey(x, y, z)

		return chunk.multiblockManager[key]
	}

	fun getMultiblockEntity(key: BlockKey): MultiblockEntity? {
		val chunk = world.getChunk(getX(key).shr(4), getZ(key).shr(4)) ?: return null

		return chunk.multiblockManager[key]
	}

	fun getChunkManager(key: BlockKey): ChunkMultiblockManager? {
		return world.getChunk(getX(key).shr(4), getZ(key).shr(4))?.multiblockManager
	}

	fun getChunkManager(x: Int, y: Int, z: Int): ChunkMultiblockManager? {
		return world.getChunk(x.shr(4), z.shr(4))?.multiblockManager
	}
}
