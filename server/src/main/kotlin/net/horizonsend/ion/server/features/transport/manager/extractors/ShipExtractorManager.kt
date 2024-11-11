package net.horizonsend.ion.server.features.transport.manager.extractors

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockTypeSafe

class ShipExtractorManager(val starship: Starship) : ExtractorManager() {
	val extractors = LongOpenHashSet()

	override fun getExtractors(): List<BlockKey> {
		return extractors.toList()
	}

	override fun isExtractor(key: BlockKey): Boolean {
		return extractors.contains(key)
	}

	override fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean {
		if (ensureExtractor && getBlockTypeSafe(starship.world, x, y, z) != EXTRACTOR_TYPE) return false
		extractors.add(toBlockKey(x, y, z))
		return true
	}

	override fun removeExtractor(x: Int, y: Int, z: Int): Boolean {
		return extractors.remove(toBlockKey(x, y, z))
	}

	override fun removeExtractor(key: BlockKey): Boolean {
		return extractors.remove(key)
	}

	fun loadExtractors() {
		starship.iterateBlocks { x, y, z -> registerExtractor(x, y, z, true) }
	}

	fun releaseExtractors() {

	}
}
