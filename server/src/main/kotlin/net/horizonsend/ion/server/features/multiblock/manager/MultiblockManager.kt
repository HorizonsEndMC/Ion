package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkageManager
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

abstract class MultiblockManager(val log: Logger) {
	/** All the loaded multiblock entities of this chunk */
	protected open val multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()

	/** All the ticked multiblock entities of this chunk */
	open val syncTickingMultiblockEntities: ConcurrentHashMap<Long, SyncTickingMultiblockEntity> = ConcurrentHashMap()
	open val asyncTickingMultiblockEntities: ConcurrentHashMap<Long, AsyncTickingMultiblockEntity> = ConcurrentHashMap()

	abstract val world: World

	abstract fun getInputManager(): InputManager

	abstract fun getLinkageManager(): MultiblockLinkageManager

	abstract fun getTransportManager(): TransportManager<*>

	abstract fun save()

	abstract fun markChanged()

	fun getAllMultiblockEntities() = multiblockEntities

	abstract fun getNetwork(type: CacheType): TransportCache

	abstract fun getSignUnsavedTime(): Long

	open fun markSignSaved() {}

	/**
	 * Handles the addition of a multiblock entity
	 **/
	fun addMultiblockEntity(entity: MultiblockEntity, save: Boolean = true, ensureSign: Boolean = false) {
		if (ensureSign) {
			val signOrigin = MultiblockEntity.getSignFromOrigin(entity.world, entity.globalVec3i, entity.structureDirection)
			if (!signOrigin.type.isWallSign) {
				log.info("Removing invalid multiblock entity at ${entity.globalVec3i} on ${entity.world.name}")
				entity.remove()
				return
			}
		}

		multiblockEntities.remove(entity.localBlockKey)?.processRemoval()
		multiblockEntities[entity.localBlockKey] = entity

		entity.processLoad()

		if (entity is SyncTickingMultiblockEntity) {
			syncTickingMultiblockEntities[entity.localBlockKey] = entity
		}

		if (entity is AsyncTickingMultiblockEntity) {
			asyncTickingMultiblockEntities[entity.localBlockKey] = entity
		}

		if (save) save()
	}

	/**
	 * Upon the removal of a multiblock sign
	 **/
	fun removeMultiblockEntity(x: Int, y: Int, z: Int, save: Boolean = true): MultiblockEntity? {
		val key = toBlockKey(x, y, z)

		val entity = multiblockEntities.remove(key)

		syncTickingMultiblockEntities.remove(key)
		asyncTickingMultiblockEntities.remove(key)

		entity?.processRemoval()

		if (save) save()

		return entity
	}

	/**
	 * Add a new multiblock to the chunk data
	 **/
	fun handleNewMultiblockEntity(multiblock: EntityMultiblock<*>, x: Int, y: Int, z: Int, face: BlockFace): MultiblockEntity? {
		multiblock as Multiblock

		// Abort process if one already exists, so it's not overwritten
		if (isOccupied(x, y, z)) {
			log.warn("Attempted to place a multiblock where one already existed!")
			return null
		}

		// Create new empty data
		val entity = multiblock.createEntity(
			this,
			PersistentMultiblockData(x, y, z, multiblock, face),
			world,
			x, y, z,
			face
		)

		// Place the entity into the chunk
		addMultiblockEntity(entity)

		entity.saveToSign()

		return entity
	}

	/**
	 * Get multiblock entity at these coordinates
	 **/
	operator fun get(x: Int, y: Int, z: Int): MultiblockEntity? {
		val key = toBlockKey(x, y, z)

		return multiblockEntities[key]
	}

	/**
	 * Get multiblock entity at this block key
	 **/
	operator fun get(key: Long): MultiblockEntity? {
		return multiblockEntities[key]
	}

	/**
	 * Multiblock entities are stored on the block the sign is placed on
	 **/
	operator fun get(sign: Sign): MultiblockEntity? {
		return multiblockEntities[getRelative(toBlockKey(sign.x, sign.y, sign.z), sign.getFacing().oppositeFace)]
	}

	fun isOccupied(x: Int, y: Int, z: Int): Boolean = multiblockEntities.containsKey(toBlockKey(x, y, z))

	fun handleTransferTo(oldBlockKey: BlockKey, newBlockKey: BlockKey, destination: MultiblockManager) {
		val atLocation = get(oldBlockKey) ?: return

		// Remove all ties to old manager and other multis
		atLocation.releaseInputs()
		atLocation.removeLinkages()

		// Transfer manager
		atLocation.manager = destination

		// Remove current occupant at spot to handle it properly
		multiblockEntities.remove(oldBlockKey)

		destination.multiblockEntities[newBlockKey] = atLocation
		if (oldBlockKey != newBlockKey) {
			atLocation.localOffsetX = getX(newBlockKey)
			atLocation.localOffsetY = getY(newBlockKey)
			atLocation.localOffsetZ = getZ(newBlockKey)
		}

		// Tie into new network
		atLocation.registerInputs()
		atLocation.linkages.forEach { t -> t.register() }

		if (atLocation is SyncTickingMultiblockEntity) {
			destination.syncTickingMultiblockEntities[newBlockKey] = atLocation
			syncTickingMultiblockEntities.remove(oldBlockKey)
		}

		if (atLocation is AsyncTickingMultiblockEntity) {
			destination.asyncTickingMultiblockEntities[newBlockKey] = atLocation
			asyncTickingMultiblockEntities.remove(oldBlockKey)
		}
	}

	open fun getGlobalCoordinate(localVec3i: Vec3i): Vec3i = localVec3i
	open fun getLocalCoordinate(globalVec3i: Vec3i): Vec3i = globalVec3i

	open fun getGlobalMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? =
		MultiblockEntities.getMultiblockEntity(world, x, y, z)
}

