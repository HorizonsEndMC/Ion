package net.horizonsend.ion.server.features.transport.manager.extractors

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.extractor.CustomExtractorBlock
import net.horizonsend.ion.server.features.transport.manager.extractors.data.AdvancedExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.transport.util.getBlockEntity
import net.horizonsend.ion.server.features.transport.util.getPersistentDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData

abstract class ExtractorManager {
	abstract fun getExtractors(): Collection<ExtractorData>

	fun registerExtractor(key: BlockKey): ExtractorData? = registerExtractor(getX(key), getY(key), getZ(key))
	abstract fun registerExtractor(x: Int, y: Int, z: Int): ExtractorData?

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

	abstract fun getExtractorData(key: BlockKey): ExtractorData?

	companion object {
		val STANDARD_EXTRACTOR_TYPE = Material.CRAFTING_TABLE
		fun isExtractorData(data: BlockData): Boolean = data.material == STANDARD_EXTRACTOR_TYPE || CustomBlocks.getByBlockData(data) is CustomExtractorBlock<*>

		fun getExtractorData(data: BlockData, pos: BlockKey, world: World): ExtractorData? {
			if (data.material == STANDARD_EXTRACTOR_TYPE) return ExtractorData.StandardExtractorData(pos)

			val customBlock = CustomBlocks.getByBlockData(data)
			if (customBlock is CustomExtractorBlock<*>) {
				val pdc = getPersistentDataContainer(pos, world)
				val stored = pdc?.get(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer)

				if (stored != null) {
					return customBlock.load(stored)
				}

				return customBlock.createExtractorData(pos)
			}

			return null
		}

		fun saveExtractor(world: World, x: Int, y: Int, z: Int, data: AdvancedExtractorData<*>) {
			val entity = getBlockEntity(Vec3i(x, y, z), world) ?: return
			entity.persistentDataContainer.set(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer, data.asMetaDataContainer())
		}
	}

	open fun onLoad() {}
	open fun save() {}
}
