package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.transport.manager.ChunkTransportManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe
import org.bukkit.persistence.PersistentDataType

class ChunkExtractorManager(val manager: ChunkTransportManager) : ExtractorManager() {
	var extractors = Long2ObjectOpenHashMap<ExtractorData>()

	private val mutex = Any()

	override fun getExtractors(): Collection<ExtractorData> = synchronized(mutex) {
		return extractors.values
	}

	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean = synchronized(mutex) {
		if (ensureExtractor && getBlockTypeSafe(manager.chunk.world, x, y, z) != EXTRACTOR_TYPE) return false
		if (!manager.chunk.isInBounds(x, y, z)) return false
		val key = toBlockKey(x, y, z)
		extractors[key] = ExtractorData(key)
		return true
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData? = synchronized(mutex) {
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): ExtractorData? = synchronized(mutex) {
		return extractors.remove(key)
	}

	override fun isExtractor(key: BlockKey): Boolean = synchronized(mutex) {
		return extractors.contains(key)
	}

	private val pdc get() = manager.chunk.inner.persistentDataContainer

	override fun onLoad() = synchronized(mutex) {
		val existing = pdc.get(NamespacedKeys.EXTRACTORS, PersistentDataType.LONG_ARRAY) ?: return
		extractors = existing.associateWithTo(Long2ObjectOpenHashMap()) { ExtractorData(it) }
	}

	override fun save() {
		pdc.set(NamespacedKeys.EXTRACTORS, PersistentDataType.LONG_ARRAY, extractors.keys.toLongArray())
	}
}
