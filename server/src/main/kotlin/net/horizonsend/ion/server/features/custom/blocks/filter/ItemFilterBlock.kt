package net.horizonsend.ion.server.features.custom.blocks.filter

import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.custom.filter.ItemFilterGui
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta.ItemFilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.CreakingHeart
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

object ItemFilterBlock : CustomFilterBlock<ItemStack, ItemFilterMeta>(
	identifier = "ITEM_FILTER",
	blockData = Material.CREAKING_HEART.createBlockData { t ->
		t as CreakingHeart
		t.axis = Axis.Z
		t.isNatural = false
		t.isActive = false
	},
	drops = BlockLoot(
		requiredTool = { BlockLoot.Tool.PICKAXE },
		drops = customItemDrop(CustomItemRegistry::ITEM_FILTER)
	),
	customBlockItem = CustomItemRegistry::ITEM_FILTER,
) {
	override fun createData(pos: BlockKey): FilterData<ItemStack, ItemFilterMeta> {
		return FilterData<ItemStack, ItemFilterMeta>(pos, FilterType.ItemType)
	}

	override fun getGui(
		player: Player,
		block: Block,
		filterData: FilterData<ItemStack, ItemFilterMeta>,
		tileState: Supplier<org.bukkit.block.TileState>
	): GuiWrapper {
		return ItemFilterGui(player, filterData, tileState)
	}
}
