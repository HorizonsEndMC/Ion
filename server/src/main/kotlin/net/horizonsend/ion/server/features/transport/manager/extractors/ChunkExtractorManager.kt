package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import org.bukkit.persistence.PersistentDataType

class ChunkExtractorManager(val manager: ChunkTransportManager) : ExtractorManager() {
	val extractors = Long2ObjectOpenHashMap<ExtractorData>()

	var needsSave: Boolean = false

	private val mutex = Any()

	override fun getExtractors(): Collection<ExtractorData> = synchronized(mutex) {
		return extractors.values
	}

	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean {
		if (ensureExtractor && getBlockTypeSafe(manager.chunk.world, x, y, z) != STANDARD_EXTRACTOR_TYPE) return false
		if (!manager.chunk.isInBounds(x, y, z)) {
			IonServer.slF4JLogger.warn("Extractor manager of ${manager.chunk} tried to register an extractor outside its bounds!")
			return false
		}
		val key = toBlockKey(x, y, z)

		synchronized(mutex) {
			extractors[key] = ExtractorData(key)
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
		val existing = manager.chunk.inner.persistentDataContainer.get(NamespacedKeys.EXTRACTORS, PersistentDataType.LONG_ARRAY)

		if (existing == null) {
			loadFromChunk()
			return
		}

		synchronized(mutex) { existing.associateWithTo(extractors) { ExtractorData(it) } }
	}

	override fun save() {
		manager.chunk.inner.persistentDataContainer.set(NamespacedKeys.EXTRACTORS, PersistentDataType.LONG_ARRAY, extractors.keys.toLongArray())
	}

	private fun loadFromChunk() = Tasks.async {
		val snapshot = manager.chunk.inner.chunkSnapshot
		val minBlockX = snapshot.x.shl(4)
		val minBlockZ = snapshot.z.shl(4)

		for (x in 0..15) for (z in 0..15) for (y in manager.chunk.world.minHeight..snapshot.getHighestBlockYAt(x, z)) {
			if (snapshot.getBlockType(x, y, z) != STANDARD_EXTRACTOR_TYPE) continue
			val key = toBlockKey(x + minBlockX, y, z + minBlockZ)
			extractors[key] = ExtractorData(key)
		}


		save()
	}
}
