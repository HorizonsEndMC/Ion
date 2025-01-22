package net.horizonsend.ion.server.features.custom.blocks.extractor

import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.mushroomBlockData
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.NORTH

object AdvancedItemExtractorBlock : CustomExtractorBlock(
	"ADVANCED_ITEM_EXTRACTOR",
	mushroomBlockData(setOf(NORTH, DOWN, NORTH)),
	BlockLoot(
		requiredTool = { BlockLoot.Tool.PICKAXE },
		drops = customItemDrop(CustomItemRegistry::ADVANCED_ITEM_EXTRACTOR)
	),
	CustomItemRegistry::ADVANCED_ITEM_EXTRACTOR,
	{ block, event -> }
) {
	override fun createExtractorData(pos: BlockKey): ExtractorData {
		return ItemExtractorData(pos, ItemExtractorData.ItemExtractorMetaData(pos))
	}
}
