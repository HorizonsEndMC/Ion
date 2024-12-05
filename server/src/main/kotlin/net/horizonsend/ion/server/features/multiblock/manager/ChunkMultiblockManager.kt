package net.horizonsend.ion.server.features.multiblock.manager

import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.MultiblockTicking
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkageManager
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputManager
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORED_MULTIBLOCK_ENTITIES
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.STORED_MULTIBLOCK_ENTITIES_OLD
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.World
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType.TAG_CONTAINER_ARRAY
import org.slf4j.Logger

class ChunkMultiblockManager(val chunk: IonChunk, log: Logger) : MultiblockManager(log) {
	override val world: World = chunk.world
	override fun getInputManager(): InputManager = chunk.world.ion.inputManager
	override fun getLinkageManager(): MultiblockLinkageManager = chunk.world.ion.multiblockManager.linkageManager

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

	override fun getNetwork(type: CacheType): TransportCache {
		return type.get(chunk)
	}

	init {
		loadMultiblocks()
		MultiblockTicking.registerMultiblockManager(this)
	}

	/**
	 * Save the multiblock data back into the chunk
	 **/
	private fun saveMultiblocks(adapterContext: PersistentDataAdapterContext) {
		val previous = chunk.inner.persistentDataContainer.get(STORED_MULTIBLOCK_ENTITIES, TAG_CONTAINER_ARRAY)
		if (previous != null) chunk.inner.persistentDataContainer.set(STORED_MULTIBLOCK_ENTITIES_OLD, TAG_CONTAINER_ARRAY, previous)

		val array = multiblockEntities.values.map { it.serialize(adapterContext, it.store()) }.toTypedArray()

		chunk.inner.persistentDataContainer.set(STORED_MULTIBLOCK_ENTITIES, TAG_CONTAINER_ARRAY, array)
	}

	/**
	 * Load the multiblocks from the persistent data container upon chunk load.
	 **/
	private fun loadMultiblocks() {
		val serialized = try {
			chunk.inner.persistentDataContainer.get(STORED_MULTIBLOCK_ENTITIES, TAG_CONTAINER_ARRAY) ?: return
		} catch (e: IllegalArgumentException) {
			log.warn("Could not load chunks multiblocks for $chunk")
			if (e.message == "The found tag instance (NBTTagList) cannot store List") {
				log.info("Found outdated list tag, removing")

				chunk.inner.persistentDataContainer.remove(STORED_MULTIBLOCK_ENTITIES)
			}

			arrayOf()
		} catch (e: Throwable) {
			// Try to load backup
			chunk.inner.persistentDataContainer.get(STORED_MULTIBLOCK_ENTITIES_OLD, TAG_CONTAINER_ARRAY) ?: return
		} catch (e: Throwable) {
			// Give up
			return
		}

		for (serializedMultiblockData in serialized) {
			val stored = PersistentMultiblockData.fromPrimitive(serializedMultiblockData, chunk.inner.persistentDataContainer.adapterContext)

			val multiblock = stored.type as EntityMultiblock<*>

			val entity = MultiblockEntities.loadFromData(multiblock, this, stored)

			// No need to save a load
			addMultiblockEntity(entity, save = false, ensureSign = true)
		}
	}

	fun onUnload() {
		multiblockEntities.values.forEach {
			it.releaseInputs()
			it.onUnload()

			if (it is DisplayMultiblockEntity) it.displayHandler.remove()
		}

		MultiblockTicking.removeMultiblockManager(this)
	}
}
