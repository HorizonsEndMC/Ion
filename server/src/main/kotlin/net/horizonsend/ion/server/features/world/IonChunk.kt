package net.horizonsend.ion.server.features.world

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.TickingMultiblock
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.toBlockKey
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY
import java.util.concurrent.ConcurrentHashMap

class IonChunk(private val inner: Chunk) {
	val locationKey = inner.chunkKey

	//TODO
	// - Wires
	// - Ore upgrade
	// -


	/**
	 * Logic upon loading the chunk
	 **/
	fun onLoad() {
		loadMultiblocks()
	}

	/**
	 * Logic upon unloading the chunk
	 **/
	fun onUnload() {
		save()
	}

	/**
	 * Logic upon world save
	 **/
	fun save() {
		saveMultiblocks()
	}

	/**
	 * Logic upon world tick
	 **/
	fun tick() {
		tickMultiblocks()
	}

	companion object : SLEventListener() {
		@EventHandler
		fun onChunkLoad(event: ChunkLoadEvent) {
			registerChunk(event.chunk)
		}

		@EventHandler
		fun onChunkUnload(event: ChunkUnloadEvent) {
			unregisterChunk(event.chunk)
		}

		/**
		 * Handles the creation, registration and loading of the chunk in the IonWorld
		 **/
		private fun registerChunk(chunk: Chunk): IonChunk {
			val ionWorld = chunk.world.ion

			val ionChunk = IonChunk(chunk)
			ionWorld.addChunk(ionChunk)

			ionChunk.onLoad()

			return ionChunk
		}

		/**
		 * Handles the unloading of the chunk
		 **/
		private fun unregisterChunk(chunk: Chunk) {
			val ionWorld = chunk.world.ion

			val removed = ionWorld.removeChunk(chunk) ?: return

			removed.onUnload()
		}

		/**
		 * Returns the chunk at the specified coordinates in the world if it is loaded
		 **/
		operator fun get(world: World, x: Int, z: Int): IonChunk? {
			return world.ion.getChunk(x, z)
		}
	}

	private val multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()
	private val tickingMultiblocks: ConcurrentHashMap<Long, TickingMultiblock> = ConcurrentHashMap()

	/**
	 * Add a new multiblock to the chunk data
	 **/
	fun addMultiblock(multiblock: Multiblock, x: Int, y: Int, z: Int) {
		val key = toBlockKey(x, y, z)

		if (multiblock is TickingMultiblock) {
			tickingMultiblocks[key] = multiblock
		}

		if (multiblock !is EntityMultiblock<*>) return

		val data = PersistentMultiblockData(x, y, z, multiblock)

		val entity = multiblock.createEntity(data, inner.world, x, y, z)

		multiblockEntities[key] = entity
	}

	fun getMultiblockEntity(x: Int, y: Int, z: Int): MultiblockEntity? {
		val key = toBlockKey(x, y, z)

		return multiblockEntities[key]
	}

	private fun tickMultiblocks() {
		tickingMultiblocks.values.forEach {
			if (it.tickAsync) {
				Multiblocks.context.launch { it.tick() }
			} else {
				it.tick()
			}
		}
	}

	/**
	 * Load the multiblocks from the persistent data container upon chunk load.
	 **/
	private fun loadMultiblocks() {
		val serialized = inner.persistentDataContainer.get(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, TAG_CONTAINER_ARRAY) ?: return

		for (serializedMultiblockData in serialized) {
			val stored = PersistentMultiblockData.fromPrimitive(serializedMultiblockData, inner.persistentDataContainer.adapterContext)

			val multiblock = stored.type as EntityMultiblock<*>

			val entity = multiblock.createEntity(stored, inner.world, stored.x, stored.y, stored.z)

			loadMultiblockEntity(entity)
		}
	}

	/**
	 * Loads a multiblock entity from storage
	 **/
	private fun loadMultiblockEntity(entity: MultiblockEntity) {
		val key = toBlockKey(entity.x, entity.y, entity.z)

		multiblockEntities[key] = entity
	}

	/**
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks() {
		val array = multiblockEntities.map { (_, entity) ->
			PersistentMultiblockData.toPrimitive(entity.store(), inner.persistentDataContainer.adapterContext)
		}.toTypedArray()

		inner.persistentDataContainer.set(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, TAG_CONTAINER_ARRAY, array)
	}
}
