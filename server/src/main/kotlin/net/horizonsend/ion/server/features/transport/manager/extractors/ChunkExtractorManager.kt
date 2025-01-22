package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData.StandardExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.ListMetaDataContainerType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.persistence.PersistentDataType

class ChunkExtractorManager(val manager: ChunkTransportManager) : ExtractorManager() {
	val extractors = Long2ObjectOpenHashMap<ExtractorData>()

	var needsSave: Boolean = false

	private val mutex = Any()

	override fun getExtractors(): Collection<ExtractorData> = synchronized(mutex) {
		return extractors.values
	}

	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean {
		val blockData = getBlockDataSafe(manager.chunk.world, x, y, z) ?: return false
		val key = toBlockKey(manager.getLocalCoordinate(Vec3i(x, y, z)))

		if (!manager.chunk.isInBounds(x, y, z)) {
			IonServer.slF4JLogger.warn("Extractor manager of ${manager.chunk} tried to register an extractor outside its bounds!")
			return false
		}

		val data = getExtractorData(blockData, key)
		if (data == null) return false

		synchronized(mutex) {
			extractors[key] = data
		}

		needsSave = true
		return true
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData? = synchronized(mutex) {
		needsSave = true
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): ExtractorData? = synchronized(mutex) {
		needsSave = true
		return extractors.remove(key)
	}

	override fun isExtractorPresent(key: BlockKey): Boolean = synchronized(mutex) {
		return extractors.contains(key)
	}

	override fun onLoad() {
		val standard = manager.chunk.inner.persistentDataContainer.get(NamespacedKeys.STANDARD_EXTRACTORS, PersistentDataType.LONG_ARRAY)

		val complex = runCatching { manager.chunk.inner.persistentDataContainer.get(NamespacedKeys.COMPLEX_EXTRACTORS, ListMetaDataContainerType) }
			.onFailure { exception -> IonServer.slF4JLogger.error("There was an error deserializing complex extractor data: $exception"); exception.printStackTrace() }
			.getOrNull()

		if (standard == null || complex == null) {
			loadFromChunk()
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

			loadFromChunk()
		}
	}

	override fun save() {
		val pdc = manager.chunk.inner.persistentDataContainer
		val standard = Long2ObjectOpenHashMap<ExtractorData>()
		extractors.filterTo(standard) { entry -> entry.value is StandardExtractorData }

		pdc.set(NamespacedKeys.STANDARD_EXTRACTORS, PersistentDataType.LONG_ARRAY, standard.keys.toLongArray())

		val complex = extractors.values.filterIsInstance<AdvancedExtractorData<*>>()
		val serialized = complex.map { it.asMetaDataContainer() }

		pdc.set(NamespacedKeys.COMPLEX_EXTRACTORS, ListMetaDataContainerType, serialized)
	}

	private fun loadFromChunk() = Tasks.async {
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
