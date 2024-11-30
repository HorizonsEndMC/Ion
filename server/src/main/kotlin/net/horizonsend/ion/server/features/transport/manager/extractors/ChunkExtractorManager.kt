package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
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

	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean = synchronized(mutex) {
		if (ensureExtractor && getBlockTypeSafe(manager.chunk.world, x, y, z) != EXTRACTOR_TYPE) return false
		if (!manager.chunk.isInBounds(x, y, z)) return false
		val key = toBlockKey(x, y, z)
		extractors[key] = ExtractorData(key)
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

	override fun isExtractor(key: BlockKey): Boolean = synchronized(mutex) {
		return extractors.contains(key)
	}

	override fun onLoad(): Unit = synchronized(mutex) {
		val existing = manager.chunk.inner.persistentDataContainer.get(NamespacedKeys.EXTRACTORS, PersistentDataType.LONG_ARRAY) ?: return
		existing.associateWithTo(extractors) { ExtractorData(it) }
	}

	override fun save() {
		manager.chunk.inner.persistentDataContainer.set(NamespacedKeys.EXTRACTORS, PersistentDataType.LONG_ARRAY, extractors.keys.toLongArray())
	}
}
