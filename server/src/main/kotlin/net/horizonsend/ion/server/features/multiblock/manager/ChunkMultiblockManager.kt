package net.horizonsend.ion.server.features.multiblock.manager

import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.MultiblockTicking
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.node.manager.NodeManager
import net.horizonsend.ion.server.features.transport.node.util.NetworkType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORED_MULTIBLOCK_ENTITIES
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORED_MULTIBLOCK_ENTITIES_OLD
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.World
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.slf4j.Logger

class ChunkMultiblockManager(val chunk: IonChunk, log: Logger) : MultiblockManager(log) {
	override val world: World = chunk.world

	/**
	 * Logic upon the chunk being saved
	 **/
	override fun save() {
		saveMultiblocks(chunk.inner.persistentDataContainer.adapterContext)
	}

	private var lastSaved = System.currentTimeMillis()
	override fun getSignUnsavedTime(): Long {
		return System.currentTimeMillis() - lastSaved
	}

	override fun markSignSaved() {
		lastSaved = System.currentTimeMillis()
	}

	override fun markChanged() {
		chunk.inner.minecraft.isUnsaved = true
	}

	override fun getNetwork(type: NetworkType): NodeManager<*> {
		return type.get(chunk)
	}

	init {
		loadMultiblocks()
		MultiblockTicking.registerMultiblockManager(this)
	}

	/**
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks(adapterContext: PersistentDataAdapterContext) = MultiblockAccess.multiblockCoroutineScope.launch {
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

			val entity = MultiblockEntities.loadFromData(multiblock, this, stored)

			// No need to save a load
			addMultiblockEntity(entity, save = false)
		}
	}

	fun onUnload() {
		multiblockEntities.values.forEach {
			it.onUnload()
		}

		MultiblockTicking.removeMultiblockManager(this)
	}
}
