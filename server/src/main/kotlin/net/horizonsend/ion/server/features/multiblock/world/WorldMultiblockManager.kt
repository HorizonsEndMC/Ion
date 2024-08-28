package net.horizonsend.ion.server.features.multiblock.world

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.world.IonWorld
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
}
