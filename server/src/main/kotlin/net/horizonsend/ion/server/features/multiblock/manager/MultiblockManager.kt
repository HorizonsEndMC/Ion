package net.horizonsend.ion.server.features.multiblock.manager

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.manager.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
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

	abstract fun save()

	abstract fun markChanged()

	/**
	 * Logic upon the chunk being ticked
	 **/
	fun tick() {
		tickAllMultiblocks()
	}

	fun getAllMultiblockEntities() = multiblockEntities

	abstract fun getNetwork(type: NetworkType): NodeManager

	/**
	 * Handles the addition of a multiblock entity
	 **/
	fun addMultiblockEntity(entity: MultiblockEntity, save: Boolean = true) {
		multiblockEntities[entity.locationKey] = entity

		entity.onLoad()

		if (entity is SyncTickingMultiblockEntity) {
			syncTickingMultiblockEntities[entity.locationKey] = entity
		}

		if (entity is AsyncTickingMultiblockEntity) {
			asyncTickingMultiblockEntities[entity.locationKey] = entity
		}

		if (entity is PoweredMultiblockEntity) {
			(getNetwork(NetworkType.POWER) as PowerNodeManager).tryBindPowerNode(entity)
		}

		if (save) save()
	}

	/**
	 * Upon the removal of a multiblock sign
	 **/
	fun removeMultiblockEntity(x: Int, y: Int, z: Int): MultiblockEntity? {
		val key = toBlockKey(x, y, z)

		val entity = multiblockEntities.remove(key)
		entity?.removed = true

		syncTickingMultiblockEntities.remove(key)
		asyncTickingMultiblockEntities.remove(key)

		entity?.handleRemoval()

		return entity
	}

	private fun tickAllMultiblocks() {
		for ((key, syncTicking) in syncTickingMultiblockEntities) runCatching {
			if (SyncTickingMultiblockEntity.preTick(syncTicking as MultiblockEntity)) syncTicking.tick()
		}.onFailure { e ->
			log.warn("Exception ticking multiblock ${syncTicking.javaClass.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}

		for ((key, asyncTicking) in asyncTickingMultiblockEntities) runCatching {
			MultiblockAccess.multiblockCoroutineScope.launch {
				if (SyncTickingMultiblockEntity.preTick(asyncTicking as MultiblockEntity)) asyncTicking.tickAsync()
			}
		}.onFailure { e ->
			log.warn("Exception ticking async multiblock ${asyncTicking.javaClass.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}
	}

	/**
	 * Add a new multiblock to the chunk data
	 **/
	suspend fun handleNewMultiblockEntity(multiblock: EntityMultiblock<*>, x: Int, y: Int, z: Int, face: BlockFace) {
		// Allow smart cast
		multiblock as Multiblock

		// Abort process if one already exists, so it's not overwritten
		if (isOccupied(x, y, z)) {
			log.warn("Attempted to place a multiblock where one already existed!")
			return
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
}

