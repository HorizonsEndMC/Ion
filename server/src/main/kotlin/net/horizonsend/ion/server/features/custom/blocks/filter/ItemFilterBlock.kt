package net.horizonsend.ion.server.features.custom.blocks.filter

import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.custom.filter.ItemFilterGui
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta.ItemFilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.block.data.type.CreakingHeart
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

object ItemFilterBlock : CustomBlock(
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
), CustomFilterBlock<ItemStack, ItemFilterMeta> {
	override fun createData(pos: BlockKey): FilterData<ItemStack, ItemFilterMeta> {
		return FilterData<ItemStack, ItemFilterMeta>(pos, FilterType.ItemType)
	}

	override fun getGui(
		player: Player,
		block: Block,
		filterData: FilterData<ItemStack, ItemFilterMeta>,
		tileState: Supplier<TileState>
	): GuiWrapper {
		return ItemFilterGui(player, filterData, tileState)
	}

	override fun placeCallback(placedItem: ItemStack, block: Block) {
		val storedFilterData = placedItem.persistentDataContainer.get(NamespacedKeys.FILTER_DATA, FilterData) ?: return

		val state = block.state
		if (state !is TileState) return

		state.persistentDataContainer.set(NamespacedKeys.FILTER_DATA, FilterData, storedFilterData)
		state.update()
	}
}
