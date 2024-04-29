package net.horizonsend.ion.server.features.multiblock

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.TickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class ChunkMultiblockManager(val chunk: IonChunk) {
	private val log = LoggerFactory.getLogger("ChunkMutliblockManager[$chunk]")

	/** All the loaded multiblock entities of this chunk */
	private val multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()

	/** All the ticked multiblock entities of this chunk */
	private val tickingMultiblockEntities: ConcurrentHashMap<Long, TickingMultiblockEntity> = ConcurrentHashMap()

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

	private fun tickAllMultiblocks() = tickingMultiblockEntities.forEachValue(tickingMultiblockEntities.size.toLong(), ::handleMultiblockTick)

	private fun handleMultiblockTick(multiblock: TickingMultiblockEntity): Boolean = try {
		if (multiblock.tickAsync) {
			Multiblocks.multiblockCoroutineScope.launch { multiblock.tick() }
		} else runBlocking { multiblock.tick() }

		true
	} catch (e: Throwable) {
		log.warn("Exception ticking multiblock ${e.message}")
		e.printStackTrace()

		false
	}

	/**
	 * Add a new multiblock to the chunk data
	 **/
	fun addNewMultiblockEntity(multiblock: EntityMultiblock<*>, sign: Sign) {
		// Allow smart cast
		multiblock as Multiblock

		val (x, y, z) = Multiblock.getOrigin(sign)
		val signOffset = sign.getFacing()

		if (isOccupied(x, y, z)) {
			log.warn("Attempted to place a multiblock where one already existed!")
			return
		}

		// Create new empty data
		val entity = multiblock.createEntity(
			PersistentMultiblockData(x, y, z, multiblock, signOffset),
			chunk.inner.world,
			x, y, z,
			signOffset
		)

		// Place the entity into the chunk
		addMultiblockEntity(entity)
	}

	/**
	 * Handles the addition of a multiblock entity
	 **/
	fun addMultiblockEntity(entity: MultiblockEntity, save: Boolean = true) {
		multiblockEntities[entity.locationKey] = entity

		if (entity is TickingMultiblockEntity) {
			tickingMultiblockEntities[entity.locationKey] = entity
		}

		if (save) saveMultiblocks(chunk.inner.persistentDataContainer.adapterContext)
	}

	/**
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks(adapterContext: PersistentDataAdapterContext) = Multiblocks.multiblockCoroutineScope.launch {
		val array = multiblockEntities.map { (_, entity) ->
			entity.serialize(adapterContext, entity.store())
		}.toTypedArray()

		chunk.inner.persistentDataContainer.set(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, PersistentDataType.TAG_CONTAINER_ARRAY, array)
	}

	/**
	 * Load the multiblocks from the persistent data container upon chunk load.
	 **/
	private fun loadMultiblocks() {
		val serialized = try {
			chunk.inner.persistentDataContainer.get(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, PersistentDataType.TAG_CONTAINER_ARRAY) ?: return
		} catch (e: IllegalArgumentException) {
			log.warn("Could not load chunks multiblocks for $chunk")
			if (e.message == "The found tag instance (NBTTagList) cannot store List") {
				log.info("Found outdated list tag, removing")

				chunk.inner.persistentDataContainer.remove(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES)
			}

			arrayOf<PersistentDataContainer>()
		}

		for (serializedMultiblockData in serialized) {
			val stored = PersistentMultiblockData.fromPrimitive(serializedMultiblockData, chunk.inner.persistentDataContainer.adapterContext)

			val multiblock = stored.type as EntityMultiblock<*>

			val entity = multiblock.createEntity(stored, chunk.inner.world, stored.x, stored.y, stored.z, stored.signOffset)

			// No need to save a load
			addMultiblockEntity(entity, save = false)
		}
	}

	/**
	 * Upon the removal of a multiblock sign
	 **/
	fun removeMultiblockEntity(x: Int, y: Int, z: Int) {
		val key = toBlockKey(x, y, z)

		multiblockEntities.remove(key)
		tickingMultiblockEntities.remove(key)
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

	fun isOccupied(x: Int, y: Int, z: Int): Boolean = multiblockEntities.containsKey(toBlockKey(x, y, z))
}
