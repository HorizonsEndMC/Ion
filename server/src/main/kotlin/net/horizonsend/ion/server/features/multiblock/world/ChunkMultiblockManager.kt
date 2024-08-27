package net.horizonsend.ion.server.features.multiblock.world

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.AsyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORED_MULTIBLOCK_ENTITIES
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORED_MULTIBLOCK_ENTITIES_OLD
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class ChunkMultiblockManager(val chunk: IonChunk) {
	private val log = LoggerFactory.getLogger("ChunkMutliblockManager[$chunk]")

	/** All the loaded multiblock entities of this chunk */
	private val multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()

	/** All the ticked multiblock entities of this chunk */
	private val syncTickingMultiblockEntities: ConcurrentHashMap<Long, SyncTickingMultiblockEntity> = ConcurrentHashMap()
	private val asyncTickingMultiblockEntities: ConcurrentHashMap<Long, AsyncTickingMultiblockEntity> = ConcurrentHashMap()

	/**
	 * Logic upon the chunk being ticked
	 **/
	fun tick() {
		tickAllMultiblocks()
	}

	/**
	 * Logic upon the chunk being saved
	 **/
	fun save() {
		saveMultiblocks(chunk.inner.persistentDataContainer.adapterContext)
	}

	init {
		loadMultiblocks()
	}

	fun getAllMultiblockEntities() = multiblockEntities

	private fun tickAllMultiblocks() {
		for ((key, syncTicking) in syncTickingMultiblockEntities) runCatching {
			syncTicking.tick()
		}.onFailure { e ->
			log.warn("Exception ticking multiblock ${syncTicking.javaClass.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}

		for ((key, asyncTicking) in asyncTickingMultiblockEntities) runCatching {
			Multiblocks.multiblockCoroutineScope.launch { asyncTicking.tickAsync() }
		}.onFailure { e ->
			log.warn("Exception ticking async multiblock ${asyncTicking.javaClass.simpleName} at ${toVec3i(key)}: ${e.message}")
			e.printStackTrace()
		}
	}

	/**
	 * Add a new multiblock to the chunk data
	 **/
	suspend fun addNewMultiblockEntity(multiblock: EntityMultiblock<*>, x: Int, y: Int, z: Int, face: BlockFace) {
		// Allow smart cast
		multiblock as Multiblock

		if (isOccupied(x, y, z)) {
			log.warn("Attempted to place a multiblock where one already existed!")
			return
		}

		// Create new empty data
		val entity = multiblock.createEntity(
			this,
			PersistentMultiblockData(x, y, z, multiblock, face),
			chunk.inner.world,
			x, y, z,
			face
		)

		// Place the entity into the chunk
		addMultiblockEntity(entity)

		if (entity is PoweredMultiblockEntity) {
			chunk.transportNetwork.powerNetwork.network.handleNewPoweredMultiblock(entity)
		}
	}

	/**
	 * Handles the addition of a multiblock entity
	 **/
	fun addMultiblockEntity(entity: MultiblockEntity, save: Boolean = true) {
		multiblockEntities[entity.locationKey] = entity

		if (entity is SyncTickingMultiblockEntity) {
			syncTickingMultiblockEntities[entity.locationKey] = entity
		}

		if (entity is AsyncTickingMultiblockEntity) {
			asyncTickingMultiblockEntities[entity.locationKey] = entity
		}

		if (save) saveMultiblocks(chunk.inner.persistentDataContainer.adapterContext)
	}

	/**
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks(adapterContext: PersistentDataAdapterContext) = Multiblocks.multiblockCoroutineScope.launch {
		val old = chunk.inner.persistentDataContainer.get(STORED_MULTIBLOCK_ENTITIES, PersistentDataType.TAG_CONTAINER_ARRAY)
		old?.let {
			chunk.inner.persistentDataContainer.set(STORED_MULTIBLOCK_ENTITIES_OLD, PersistentDataType.TAG_CONTAINER_ARRAY, it)
		}

		val array = multiblockEntities.map { (_, entity) ->
			entity.serialize(adapterContext, entity.store())
		}.toTypedArray()

		chunk.inner.persistentDataContainer.set(STORED_MULTIBLOCK_ENTITIES, PersistentDataType.TAG_CONTAINER_ARRAY, array)
	}

	/**
	 * Load the multiblocks from the persistent data container upon chunk load.
	 **/
	private fun loadMultiblocks() {
		val serialized = try {
			chunk.inner.persistentDataContainer.get(STORED_MULTIBLOCK_ENTITIES, PersistentDataType.TAG_CONTAINER_ARRAY) ?: return
		} catch (e: IllegalArgumentException) {
			log.warn("Could not load chunks multiblocks for $chunk")
			if (e.message == "The found tag instance (NBTTagList) cannot store List") {
				log.info("Found outdated list tag, removing")

				chunk.inner.persistentDataContainer.remove(STORED_MULTIBLOCK_ENTITIES)
			}

			arrayOf()
		} catch (e: Throwable) {
			// Try to load backup
			chunk.inner.persistentDataContainer.get(STORED_MULTIBLOCK_ENTITIES_OLD, PersistentDataType.TAG_CONTAINER_ARRAY) ?: return
		} catch (e: Throwable) {
			// Give up
			return
		}

		for (serializedMultiblockData in serialized) {
			val stored = PersistentMultiblockData.fromPrimitive(serializedMultiblockData, chunk.inner.persistentDataContainer.adapterContext)

			val multiblock = stored.type as EntityMultiblock<*>

			val entity = multiblock.createEntity(this, stored, chunk.inner.world, stored.x, stored.y, stored.z, stored.signOffset)

			// No need to save a load
			addMultiblockEntity(entity, save = false)
		}
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
