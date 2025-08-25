package net.horizonsend.ion.server.features.multiblock.manager

import kotlinx.serialization.SerializationException
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.multiblock.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.MultiblockTicking
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkageManager
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.transport.inputs.IOManager
import net.horizonsend.ion.server.features.transport.manager.TransportManager
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
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
	override fun getInputManager(): IOManager = chunk.world.ion.inputManager
	override fun getLinkageManager(): MultiblockLinkageManager = chunk.world.ion.multiblockManager.linkageManager
	override fun getTransportManager(): TransportManager<*> = chunk.transportNetwork

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
		chunk.inner.minecraft.markUnsaved()
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
			val stored = runCatching {
				PersistentMultiblockData.fromPrimitive(serializedMultiblockData, chunk.inner.persistentDataContainer.adapterContext)
			}.onFailure { exception ->
				if (exception is SerializationException) {
					log.warn("Could not load multiblock, skipping.")
				}
			}.getOrNull() ?: continue

			val multiblock = stored.type as EntityMultiblock<*>

			val entity = try {
				MultiblockEntities.loadFromData(multiblock, this, stored)
			}
			catch (e: Throwable) {
				if (ConfigurationFiles.serverConfiguration().deleteInvalidMultiblockData) {
					log.warn("Removed invalid multiblock entity!", e)
					continue
				} else throw e
			}

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

	override fun getGlobalMultiblockEntity(world: World, x: Int, y: Int, z: Int): MultiblockEntity? {
		if (x.shr(4) == chunk.x && z.shr(4) == chunk.z) return get(x, y, z)
		return super.getGlobalMultiblockEntity(world, x, y, z)
	}
}
