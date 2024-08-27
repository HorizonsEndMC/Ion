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
		return worldMultiblocks.getOrPut(kClass) { LinkedBlockingQueue<MultiblockEntity>() }.add(multiblockEntity)
	}

	fun deregister(multiblockEntity: MultiblockEntity): Boolean {
		val kClass = multiblockEntity::class
		val success =  worldMultiblocks.getOrPut(kClass) { LinkedBlockingQueue<MultiblockEntity>() }.remove(multiblockEntity)

		if (!success) IonServer.slF4JLogger.warn("Tried to deregister multiblock entity that was not registered!")
		return success
	}

	operator fun <T : MultiblockEntity> get(type: KClass<T>): Collection<T> {
		@Suppress("UNCHECKED_CAST")
		return worldMultiblocks[type] as Collection<T>
	}
}
