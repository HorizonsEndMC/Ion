package net.horizonsend.ion.server.features.transport.manager.extractors

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import org.bukkit.Material

abstract class ExtractorManager {
	abstract fun getExtractors(): List<BlockKey>
	fun registerExtractor(key: BlockKey, ensureExtractor: Boolean): Boolean = registerExtractor(getX(key), getY(key), getZ(key), ensureExtractor)
	abstract fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean
	abstract fun isExtractor(key: BlockKey): Boolean

	companion object {
		val EXTRACTOR_TYPE = Material.CRAFTING_TABLE
	}

	open fun onLoad() {}
	open fun onSave() {}
}
