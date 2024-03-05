package net.horizonsend.ion.server.features.world

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY
import java.util.LinkedList

class IonChunk(val inner: Chunk) {
	val locationKey = inner.chunkKey

	fun onLoad() {
		loadMultiblocks()
	}

	fun onUnload() {
		save()
	}

	fun save() {
		saveMultiblocks()
	}

	private val multiblockEntities: LinkedList<MultiblockEntity> = LinkedList()

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
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks() {
		val array = multiblockEntities.map {
			PersistentMultiblockData.toPrimitive(it.store(), inner.persistentDataContainer.adapterContext)
		}.toTypedArray()

		inner.persistentDataContainer.set(NamespacedKeys.STORED_MULTIBLOCK_ENTITIES, TAG_CONTAINER_ARRAY, array)
	}

	/**
	 * Add a new multiblock to the chunk data
	 **/
	fun addMultiblock(multiblock: Multiblock, x: Int, y: Int, z: Int) {
		if (multiblock !is EntityMultiblock<*>) return

		val data = PersistentMultiblockData(x, y, z, multiblock)

		val entity = multiblock.createEntity(data, inner.world, x, y, z)

		multiblockEntities.add(entity)
	}

	fun getMultiblockEntity(x: Int, y: Int, z: Int): MultiblockEntity? {
		return multiblockEntities.firstOrNull {
			it.x == x && it.y == y && it.z == z
		}
	}

	/**
	 * Loads a multiblock entity from storage
	 **/
	private fun loadMultiblockEntity(entity: MultiblockEntity) {
		multiblockEntities.add(entity)
	}

	/**
	 * Tick the stuff in the chunk
	 **/
	fun tick() {
		multiblockEntities.forEach { it.tick() }
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
}
