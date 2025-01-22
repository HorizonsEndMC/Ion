package net.horizonsend.ion.server.features.transport.manager.extractors

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomExtractorBlock
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Material
import org.bukkit.block.data.BlockData

abstract class ExtractorManager {
	abstract fun getExtractors(): Collection<ExtractorData>

	fun registerExtractor(key: BlockKey, ensureExtractor: Boolean): Boolean = registerExtractor(getX(key), getY(key), getZ(key), ensureExtractor)
	abstract fun registerExtractor(x: Int, y: Int, z: Int, ensureExtractor: Boolean): Boolean

	abstract fun removeExtractor(key: BlockKey): ExtractorData?
	abstract fun removeExtractor(x: Int, y: Int, z: Int): ExtractorData?

	/**
	 * Returns whether an extractor is present at this location
	 **/
	fun isExtractorPresent(x: Int, y: Int, z: Int): Boolean = isExtractorPresent(toBlockKey(x, y, z))

	/**
	 * Returns whether an extractor is present at this location
	 **/
	abstract fun isExtractorPresent(key: BlockKey): Boolean

	companion object {
		val STANDARD_EXTRACTOR_TYPE = Material.CRAFTING_TABLE
		fun isExtractorData(data: BlockData): Boolean = data.material == STANDARD_EXTRACTOR_TYPE || CustomBlocks.getByBlockData(data) is CustomExtractorBlock

		fun getExtractorData(data: BlockData, pos: BlockKey): ExtractorData? {
			if (data.material == STANDARD_EXTRACTOR_TYPE) return ExtractorData(pos)

			val customBlock = CustomBlocks.getByBlockData(data)
			if (customBlock is CustomExtractorBlock) return customBlock.createExtractorData(pos)

			return null
		}
	}

	open fun onLoad() {}
	open fun save() {}
}
