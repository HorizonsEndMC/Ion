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
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.CommandBlock
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

object ItemFilterBlock : CustomFilterBlock<ItemStack, ItemFilterMeta>(
	identifier = "ITEM_FILTER",
	blockData = Material.COMMAND_BLOCK.createBlockData { t ->
		t as org.bukkit.block.data.type.CommandBlock
		t.facing = BlockFace.UP
		t.isConditional = true
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
		commandBlock: Supplier<CommandBlock>
	): GuiWrapper {
		return ItemFilterGui(player, filterData, commandBlock)
	}
}
