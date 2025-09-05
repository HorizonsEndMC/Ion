package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData.StandardExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.util.getPersistentDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.ListMetaDataContainerType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.persistence.PersistentDataType

class ChunkExtractorManager(val manager: ChunkTransportManager) : ExtractorManager() {
	private val extractors = Long2ObjectOpenHashMap<ExtractorData>()

	private val mutex = Any()

	var isLoading: Boolean = false

	override fun getExtractors(): Collection<ExtractorData> = synchronized(mutex) {
		return extractors.values
	}

	override fun registerExtractor(x: Int, y: Int, z: Int): ExtractorData? {
		val blockData = getBlockDataSafe(manager.chunk.world, x, y, z) ?: return null
		val key = toBlockKey(manager.getLocalCoordinate(Vec3i(x, y, z)))

		if (!manager.chunk.isInBounds(x, y, z)) {
			IonServer.slF4JLogger.warn("Extractor manager of ${manager.chunk} tried to register an extractor outside its bounds!")
			return null
		}

		val data = getExtractorData(blockData, key, manager.getWorld())
		if (data == null) return null

		synchronized(mutex) {
			extractors[key] = data
		}

		return data
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData? = synchronized(mutex) {
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): ExtractorData?  {
		return synchronized(mutex) {
			extractors.remove(key)
		}
	}

	override fun isExtractorPresent(key: BlockKey): Boolean = synchronized(mutex) {
		return extractors.contains(key)
	}

	override fun getExtractorData(key: BlockKey): ExtractorData? {
		return extractors[key]
	}

	override fun save() {
		if (isLoading) return

		val pdc = manager.chunk.inner.persistentDataContainer
		val standard = Long2ObjectOpenHashMap<ExtractorData>()
		extractors.filterTo(standard) { entry -> entry.value is StandardExtractorData }

		pdc.set(NamespacedKeys.STANDARD_EXTRACTORS, PersistentDataType.LONG_ARRAY, standard.keys.toLongArray())

		val complex = extractors.values.filterIsInstance<AdvancedExtractorData<*>>()

		val serialized = complex.map { entry ->
			val serialized = entry.asMetaDataContainer()

			val entityBackup = getPersistentDataContainer(manager.getGlobalCoordinate(toVec3i(entry.pos)), manager.getWorld())
			entityBackup?.set(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer, serialized)

			serialized
		}

		pdc.set(NamespacedKeys.COMPLEX_EXTRACTORS, ListMetaDataContainerType, serialized)
	}

	override fun onLoad() {
		isLoading = true

		try {
			val standard = manager.chunk.inner.persistentDataContainer.get(NamespacedKeys.STANDARD_EXTRACTORS, PersistentDataType.LONG_ARRAY)

			val complex = runCatching { manager.chunk.inner.persistentDataContainer.get(NamespacedKeys.COMPLEX_EXTRACTORS, ListMetaDataContainerType) }
				.onFailure { exception -> IonServer.slF4JLogger.error("There was an error deserializing complex extractor data: $exception"); exception.printStackTrace() }
				.getOrNull()

			if (standard == null || complex == null) {
				rebuildFromChunk()
				return
			}

			runCatching {
				synchronized(mutex) {
					standard.associateWithTo(extractors) { StandardExtractorData(it) }
					complex.associateTo(extractors) { it.data as ExtractorMetaData; it.data.key to it.data.toExtractorData() }
				}
			}.onFailure { exception ->
				IonServer.slF4JLogger.error("There was an error loading complex extractor data: $exception")
				exception.printStackTrace()

				rebuildFromChunk()
			}
		} finally {
		    isLoading = false
		}
	}

	private fun rebuildFromChunk() = Tasks.async {
		val snapshot = manager.chunk.inner.chunkSnapshot
		val minBlockX = snapshot.x.shl(4)
		val minBlockZ = snapshot.z.shl(4)

		for (x in 0..15) for (z in 0..15) for (y in manager.chunk.world.minHeight..snapshot.getHighestBlockYAt(x, z)) {
			if (!isExtractorData(snapshot.getBlockData(x, y, z))) continue
			val key = toBlockKey(x + minBlockX, y, z + minBlockZ)
			extractors[key] = StandardExtractorData(key)
		}

		save()
	}
}
