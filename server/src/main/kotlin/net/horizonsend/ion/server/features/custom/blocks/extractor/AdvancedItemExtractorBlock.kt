package net.horizonsend.ion.server.features.custom.blocks.extractor

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.mushroomBlockData
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.entity.Player

object AdvancedItemExtractorBlock : CustomExtractorBlock<ItemExtractorData>(
	"ADVANCED_ITEM_EXTRACTOR",
	mushroomBlockData(setOf(NORTH, DOWN, NORTH)),
	BlockLoot(
		requiredTool = { BlockLoot.Tool.PICKAXE },
		drops = customItemDrop(CustomItemRegistry::ADVANCED_ITEM_EXTRACTOR)
	),
	CustomItemRegistry::ADVANCED_ITEM_EXTRACTOR,
	ItemExtractorData::class
) {
	override fun createExtractorData(pos: BlockKey): ItemExtractorData {
		return ItemExtractorData(pos, ItemExtractorData.ItemExtractorMetaData(pos))
	}

	override fun openGUI(player: Player, block: Block, extractorData: ItemExtractorData) {
		val current = extractorData.metaData.sortingOrder
		val entires = SortingOrder.entries
		player.information("Current: $current")

		val new = if (current.ordinal + 1 > entires.lastIndex) 0 else current.ordinal + 1

		player.success("New: ${SortingOrder.entries[new]}")
		extractorData.metaData.sortingOrder = SortingOrder.entries[new]
	}
}
