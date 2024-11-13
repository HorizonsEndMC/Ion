package net.horizonsend.ion.server.features.multiblock

import org.bukkit.block.Sign as SignState
import org.bukkit.block.data.type.WallSign as SignData
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_ENTITY_DATA
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.World
import org.bukkit.block.Block
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

	/**
	 *
	 **/
	fun getMultiblockEntity(world: World, key: BlockKey): MultiblockEntity? {
		val x = getX(key)
		val y = getY(key)
		val z = getZ(key)
		val ionChunk = getIonChunk(world, x, z) ?: return null

		return ionChunk.multiblockManager[x, y, z]
	}

	fun getMultiblockEntity(origin: Block): MultiblockEntity? {
		return getMultiblockEntity(origin.world, origin.x, origin.y, origin.z)
	}

	fun getMultiblockEntity(sign: SignState): MultiblockEntity? {
		val origin = MultiblockEntity.getOriginFromSign(sign)
		return getMultiblockEntity(sign.world, origin.x, origin.y, origin.z)
	}

	fun getMultiblockEntity(signLocation: BlockKey, world: World, sign: SignData): MultiblockEntity? {
		val origin = getRelative(signLocation, sign.facing.oppositeFace)
		return getMultiblockEntity(world, getX(origin), getY(origin), getZ(origin))
	}

	fun getMultiblockEntity(signX: Int, signY: Int, signZ: Int, world: World, sign: SignData): MultiblockEntity? {
		val structureDirection = sign.facing.oppositeFace
		return getMultiblockEntity(world, signX + structureDirection.modX, signY + structureDirection.modY, signZ + structureDirection.modZ)
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

	fun loadFromSign(sign: SignState) {
		val multiblockType = MultiblockAccess.getFast(sign) as? EntityMultiblock<*> ?: return

		val data = sign.persistentDataContainer.get(MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData) ?: return migrateFromSign(sign, multiblockType)

		val origin = MultiblockEntity.getOriginFromSign(sign)

		// In case it moved
		data.x = origin.x
		data.y = origin.y
		data.z = origin.z
		data.signOffset = sign.getFacing().oppositeFace

		setMultiblockEntity(sign.world, origin.x, origin.y, origin.z) { manager ->
			val new = loadFromData(multiblockType, manager, data)
			if (new is LegacyMultiblockEntity) new.loadFromSign(sign)

			new
		}
	}

	fun migrateFromSign(sign: SignState, type: EntityMultiblock<*>) {
		val origin = MultiblockEntity.getOriginFromSign(sign)

		val ionChunk = getIonChunk(sign.world, origin.x, origin.z) ?: return
		val new = ionChunk.multiblockManager.handleNewMultiblockEntity(type, origin.x, origin.y, origin.z, sign.getFacing().oppositeFace)

		if (new is LegacyMultiblockEntity) new.loadFromSign(sign)
		new?.saveToSign()
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
