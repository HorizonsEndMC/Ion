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
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType.LIST
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
		saveMultiblocks()
	}

	init {
		loadMultiblocks()
	}

	fun getAllMultiblockEntities() = multiblockEntities

	private fun tickAllMultiblocks() = tickingMultiblockEntities.forEachValue(tickingMultiblockEntities.size.toLong(), ::handleMultiblockTick)

	private fun handleMultiblockTick(multiblock: TickingMultiblockEntity): Boolean = try {
		if (multiblock.tickAsync) {
			Multiblocks.context.launch { multiblock.tick() }
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

		if (save) saveMultiblocks()
	}

	/**
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks() = Multiblocks.context.launch {
		val array = multiblockEntities.map { (_, entity) ->
			PersistentMultiblockData.toPrimitive(entity.store(), chunk.inner.persistentDataContainer.adapterContext)
		}

		chunk.inner.persistentDataContainer.set(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, LIST.dataContainers(), array)
		chunk.inner.minecraft.isUnsaved = true
	}

	/**
	 * Load the multiblocks from the persistent data container upon chunk load.
	 **/
	private fun loadMultiblocks() {
		val serialized = chunk.inner.persistentDataContainer.get(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, LIST.dataContainers()) ?: return

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
