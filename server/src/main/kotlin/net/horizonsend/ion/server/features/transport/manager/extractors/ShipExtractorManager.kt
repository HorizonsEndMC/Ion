package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe

class ShipExtractorManager(val starship: Starship) : ExtractorManager() {
	val extractors = Long2ObjectOpenHashMap<ExtractorData>()

	override fun getExtractors(): Collection<ExtractorData> {
		return extractors.values
	}

	override fun isExtractor(key: BlockKey): Boolean {
		return extractors.contains(key)
	}

	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean {
		if (ensureExtractor && getBlockTypeSafe(starship.world, x, y, z) != EXTRACTOR_TYPE) return false
		val key = toBlockKey(x, y, z)
		extractors[key] = ExtractorData(key)
		return true
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData? {
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): ExtractorData? {
		return extractors.remove(key)
	}

	fun loadExtractors() {
		starship.iterateBlocks { x, y, z -> registerExtractor(x, y, z, true) }
	}

	fun releaseExtractors() {
		//TODO
	}
}
