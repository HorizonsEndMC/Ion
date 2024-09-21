package net.horizonsend.ion.server.features.multiblock

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_ENTITY_DATA
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler

/**
 * Provides utility functions for multiblock entities, and handles sign backups
 **/
object MultiblockEntities : SLEventListener() {
	/**
	 *
	 **/
	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = getIonChunk(world, x, z) ?: return null

		return ionChunk.multiblockManager[x, y, z]
	}

	fun getMultiblockEntity(origin: Block): MultiblockEntity? {
		return getMultiblockEntity(origin.world, origin.x, origin.y, origin.z)
	}

	/**
	 * Add a new multiblock entity to the chunk
	 **/
	fun setMultiblockEntity(world: World, x: Int, y: Int, z: Int, createEntity: (MultiblockManager) -> MultiblockEntity): MultiblockEntity? {
		val ionChunk = getIonChunk(world, x, z) ?: return null

		val manager = ionChunk.multiblockManager

		val entity = createEntity(manager)
		manager.addMultiblockEntity(entity, save = true)

		return entity
	}

	/**
	 *
	 **/
	fun removeMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = getIonChunk(world, x, z) ?: return null

		return ionChunk.multiblockManager.removeMultiblockEntity(x, y, z)
	}

	private fun getIonChunk(world: World, x: Int, z: Int): IonChunk? {
		val chunkX = x.shr(4)
		val chunkZ = z.shr(4)

		return world.ion.getChunk(chunkX, chunkZ)
	}

	fun <T: MultiblockEntity> loadFromData(multiblock: EntityMultiblock<T>, manager: MultiblockManager, stored: PersistentMultiblockData): T {
		return multiblock.createEntity(manager, stored, manager.world, stored.x, stored.y, stored.z, stored.signOffset)
	}

	fun loadFromSign(sign: Sign) {
		val data = sign.persistentDataContainer.get(MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData) ?: return
		val origin = MultiblockEntity.getOriginFromSign(sign)
		val multiblockType = MultiblockAccess.getFast(sign) as? EntityMultiblock<*> ?: return

		// In case it moved
		data.x = origin.x
		data.y = origin.y
		data.z = origin.z
		data.signOffset = sign.getFacing().oppositeFace

		setMultiblockEntity(sign.world, origin.x, origin.y, origin.z) { manager ->
			loadFromData(multiblockType, manager, data)
		}
	}

	@EventHandler
	fun onTickEnd(event: ServerTickEndEvent) {
		if (event.timeRemaining < 0) return

		val sorted = MultiblockTicking.getAllMultiblockManagers()
		for (manager in sorted) {
			if (manager.getSignUnsavedTime() < 5000L) continue

			for (keyEntity in manager.getAllMultiblockEntities()) {
				if (event.timeRemaining < 0) return
				keyEntity.value.saveToSign()
			}
			manager.markSignSaved()
		}
	}
}
