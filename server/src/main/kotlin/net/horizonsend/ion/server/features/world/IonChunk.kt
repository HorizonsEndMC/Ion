package net.horizonsend.ion.server.features.world

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.TickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.transport.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.ExtractorData
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.EXTRACTOR_DATA
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.toBlockKey
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class IonChunk(private val inner: Chunk) {
	val locationKey = inner.chunkKey

	val originX = inner.x.shl(4)
	val originZ = inner.z.shl(4)

	// TODO
	//  - Wires
	//  - Ore upgrader
	//  -

	val powerNetwork: ChunkPowerNetwork = ChunkPowerNetwork(this, getExtractorData(inner))

	/**
	 * Logic upon loading the chunk
	 **/
	fun onLoad() {
		// Load all multiblocks from persistent data
		loadMultiblocks()
		collectTickedMultiblocks()
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
		powerNetwork.tick()
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

		private fun getExtractorData(chunk: Chunk): ExtractorData {
			val extractors = chunk.persistentDataContainer.get(EXTRACTOR_DATA, ExtractorData)

			return extractors ?: ExtractorData(ConcurrentLinkedQueue())
		}

		fun Chunk.ion(): IonChunk = this.world.ion.getChunk(chunkKey)!!
	}

	private val multiblockEntities: ConcurrentHashMap<Long, MultiblockEntity> = ConcurrentHashMap()
	private val tickingMultiblockEntities: ConcurrentHashMap<Long, TickingMultiblockEntity> = ConcurrentHashMap()

	fun getAllMultiblockEntities() = multiblockEntities

	/**
	 * Add a new multiblock to the chunk data
	 **/
	fun addMultiblock(multiblock: Multiblock, sign: Sign) {
		val (x, y, z) = Multiblock.getOrigin(sign)
		val signOffset = sign.getFacing()

		val key = toBlockKey(x, y, z)

		if (multiblockEntities.containsKey(key)) {
			log.warn("Attempted to place a multiblock where one already existed!")
			return
		}

		if (multiblock is TickingMultiblockEntity) {
			tickingMultiblockEntities[key] = multiblock
		}

		if (multiblock !is EntityMultiblock<*>) return

		val data = PersistentMultiblockData(x, y, z, multiblock, signOffset)

		val entity = multiblock.createEntity(data, inner.world, x, y, z, signOffset.oppositeFace)

		multiblockEntities[key] = entity

		saveMultiblocks()
	}

	/**
	 * Upon the removal of a multiblock sign
	 **/
	fun removeMultiblock(x: Int, y: Int, z: Int) {
		val key = toBlockKey(x, y, z)

		multiblockEntities.remove(key)
		tickingMultiblockEntities.remove(key)
	}

	fun getMultiblockEntity(x: Int, y: Int, z: Int): MultiblockEntity? {
		val key = toBlockKey(x, y, z)

		return multiblockEntities[key]
	}

	private fun tickMultiblocks() {
		tickingMultiblockEntities.forEach { (key, multiblock) ->
			try {
//				multiblock as MultiblockEntity
//				if (!multiblock.isIntact()) {
//					tickingMultiblockEntities.remove(key)
//				}

				if (multiblock.tickAsync) {
					Multiblocks.context.launch { multiblock.tick() }
				} else runBlocking { multiblock.tick() }
			} catch (e: Throwable) {
				log.warn("Exception ticking multiblock ${e.message}")
				e.printStackTrace()
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

			val entity = multiblock.createEntity(stored, inner.world, stored.x, stored.y, stored.z, stored.signOffset)

			loadMultiblockEntity(entity)
		}
	}

	private fun collectTickedMultiblocks() {
		for ((location, multiblock) in multiblockEntities) {
			if (multiblock !is TickingMultiblockEntity) continue

			tickingMultiblockEntities[location] = multiblock
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
