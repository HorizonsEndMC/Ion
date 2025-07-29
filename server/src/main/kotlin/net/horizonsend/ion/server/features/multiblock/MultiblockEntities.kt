package net.horizonsend.ion.server.features.multiblock

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.ChunkMultiblockManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.MULTIBLOCK_ENTITY_DATA
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.*
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import java.util.concurrent.TimeUnit
import org.bukkit.block.Sign as SignState
import org.bukkit.block.data.type.WallSign as WallSignData

/**
 * Provides utility functions for multiblock entities, and handles sign backups
 **/
object MultiblockEntities : IonServerComponent() {
	override fun onEnable() {
		Tasks.asyncRepeat(20L, 20L, ::performCleaup)
	}

	/**
	 *
	 **/
	fun getMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = IonChunk.getFromWorldCoordinates(world, x, z) ?: return null

		return ionChunk.multiblockManager[x, y, z]
	}

	/**
	 *
	 **/
	fun getMultiblockEntity(world: World, key: BlockKey): MultiblockEntity? {
		val x = getX(key)
		val y = getY(key)
		val z = getZ(key)
		val ionChunk = IonChunk.getFromWorldCoordinates(world, x, z) ?: return null

		return ionChunk.multiblockManager[x, y, z]
	}

	fun getMultiblockEntity(origin: Block): MultiblockEntity? {
		return getMultiblockEntity(origin.world, origin.x, origin.y, origin.z)
	}

	fun getMultiblockEntity(sign: SignState): MultiblockEntity? {
		val origin = MultiblockEntity.getOriginFromSign(sign)
		return getMultiblockEntity(sign.world, origin.x, origin.y, origin.z)
	}

	fun getMultiblockEntity(signLocation: BlockKey, world: World, sign: WallSignData): MultiblockEntity? {
		val origin = getRelative(signLocation, sign.facing.oppositeFace)
		return getMultiblockEntity(world, getX(origin), getY(origin), getZ(origin))
	}

	fun getMultiblockEntity(signX: Int, signY: Int, signZ: Int, world: World, sign: WallSignData): MultiblockEntity? {
		val structureDirection = sign.facing.oppositeFace
		return getMultiblockEntity(world, signX + structureDirection.modX, signY + structureDirection.modY, signZ + structureDirection.modZ)
	}

	/**
	 * Add a new multiblock entity to the chunk
	 **/
	fun setMultiblockEntity(world: World, x: Int, y: Int, z: Int, save: Boolean = true, createEntity: (MultiblockManager) -> MultiblockEntity): MultiblockEntity? {
		val ionChunk = IonChunk.getFromWorldCoordinates(world, x, z) ?: return null

		val manager = ionChunk.multiblockManager

		val entity = createEntity(manager)
		manager.addMultiblockEntity(entity, save = save)

		return entity
	}

	/**
	 *
	 **/
	fun removeMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		val ionChunk = IonChunk.getFromWorldCoordinates(world, x, z) ?: return null

		return ionChunk.multiblockManager.removeMultiblockEntity(x, y, z)
	}

	/**
	 *
	 **/
	fun removeMultiblockEntity(world: World, signX: Int, signY: Int, signZ: Int, wallSignData: WallSignData): MultiblockEntity? {
		val (x, y, z) = Vec3i(signX, signY, signZ).getRelative(wallSignData.facing.oppositeFace)
		val ionChunk = IonChunk.getFromWorldCoordinates(world, x, z) ?: return null

		return ionChunk.multiblockManager.removeMultiblockEntity(x, y, z)
	}

	fun <T: MultiblockEntity> loadFromData(multiblock: EntityMultiblock<T>, manager: MultiblockManager, stored: PersistentMultiblockData): T {
		return multiblock.createEntity(manager, stored, manager.world, stored.x, stored.y, stored.z, stored.signOffset)
	}

	fun loadFromSign(sign: SignState, save: Boolean = true): MultiblockEntity? {
		val multiblockType = MultiblockAccess.getFast(sign)
		if (multiblockType !is EntityMultiblock<*>) return null

		val data = sign.persistentDataContainer.get(MULTIBLOCK_ENTITY_DATA, PersistentMultiblockData) ?: return migrateFromSign(sign, multiblockType)

		val origin = MultiblockEntity.getOriginFromSign(sign)

		// In case it moved
		data.x = origin.x
		data.y = origin.y
		data.z = origin.z
		data.signOffset = sign.getFacing().oppositeFace

		return setMultiblockEntity(sign.world, origin.x, origin.y, origin.z, save = save) { manager ->
			val new = loadFromData(multiblockType, manager, data)
			if (new is LegacyMultiblockEntity) {
				new.loadFromSign(sign)
			}

			new
		}
	}

	fun migrateFromSign(sign: SignState, type: EntityMultiblock<*>): MultiblockEntity? {
		val origin = MultiblockEntity.getOriginFromSign(sign)

		val ionChunk = IonChunk.getFromWorldCoordinates(sign.world, origin.x, origin.z) ?: return null
		val new = ionChunk.multiblockManager.handleNewMultiblockEntity(type, origin.x, origin.y, origin.z, sign.getFacing().oppositeFace)

		if (new is LegacyMultiblockEntity) new.loadFromSign(sign)
		new?.saveToSign()

		return new
	}

	private val msptBuffer = TimeUnit.MILLISECONDS.toNanos(5)
	private val saveInterval = TimeUnit.SECONDS.toMillis(20)

	@EventHandler
	fun onTickEnd(event: ServerTickEndEvent) {
		if (event.timeRemaining < msptBuffer) return

		MultiblockTicking.iterateManagers { manager ->
			if (manager !is ChunkMultiblockManager) return@iterateManagers
			if (manager.getSignUnsavedTime() < saveInterval) return@iterateManagers

			for (keyEntity in manager.getAllMultiblockEntities()) {
				if (event.timeRemaining < msptBuffer) {
					break
				}

				keyEntity.value.saveToSign()
			}

			manager.markSignSaved()
		}
	}

	fun performCleaup() {
		MultiblockTicking.iterateManagers {
			it.getAllMultiblockEntities().values.forEach { entity ->
				entity.cleanup()
			}
		}
	}
}
