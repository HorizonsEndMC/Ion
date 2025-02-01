package net.horizonsend.ion.server.features.custom.blocks.extractor

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.CreakingHeart
import org.bukkit.entity.Player

object AdvancedItemExtractorBlock : CustomExtractorBlock<ItemExtractorData>(
	"ADVANCED_ITEM_EXTRACTOR",
	blockData = Material.CREAKING_HEART.createBlockData { t ->
		t as CreakingHeart
		t.axis = Axis.X
		t.isNatural = false
		t.isActive = false
	},
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

		val chunk = IonChunk[block.world, block.x.shr(4), block.z.shr(4)] ?: return
		val itemCache = CacheType.ITEMS.get(chunk) as ItemTransportCache
		itemCache.handleExtractorTick(toBlockKey(block.x, block.y, block.z), 1.0, extractorData.metaData)
	}
}
