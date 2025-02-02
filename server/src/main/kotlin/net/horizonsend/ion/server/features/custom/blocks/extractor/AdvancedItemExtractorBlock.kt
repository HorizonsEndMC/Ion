package net.horizonsend.ion.server.features.custom.blocks.extractor

import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI.Companion.setTitle
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Axis
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.CreakingHeart
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

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
		AdvancedItemExtractorGUI(player, block, extractorData).open()
	}

	class AdvancedItemExtractorGUI(val viewer: Player, val block: Block, val extractorData: ItemExtractorData) : GuiWrapper {
		override fun open() {
			val gui = Gui.normal()
				.setStructure(
					"u u u u u u u u u",
					". . . . . . . . .",
					"d d d d d d d d d"
				)
				.addIngredient('u', getTraverseButton(1))
				.addIngredient('d', getTraverseButton(-1))

			Window
				.single()
				.setGui(gui)
				.setTitle(AdventureComponentWrapper(getSlotOverlay()))
				.build(viewer)
				.open()
		}

		fun getOffset(offset: Int): SortingOrder {
			val current = extractorData.metaData.sortingOrder
			val entries = SortingOrder.entries
			return SortingOrder.entries[Math.floorMod(current.ordinal + offset, entries.size)]
		}

		fun getSlotOverlay(): Component = GuiText("Item Extractor Configuration")
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #"
			)
			.add(
				component = Component.text().color(NamedTextColor.GRAY).append(getOffset(+1).displayName).build(),
				alignment = GuiText.TextAlignment.CENTER,
				line = 0,
				verticalShift = -54
			)
			.add(
				component = getOffset(+0).displayName,
				alignment = GuiText.TextAlignment.CENTER,
				line = 2,
			)
			.add(
				component = Component.text().color(NamedTextColor.GRAY).append(getOffset(-1).displayName).build(),
				alignment = GuiText.TextAlignment.CENTER,
				line = 4,
				verticalShift = +4
			)
			.build()

		fun getTraverseButton(offset: Int): AbstractItem = GuiItems.createButton(GuiItems.blankItem) { _, player, event ->
			val current = extractorData.metaData.sortingOrder
			val entires = SortingOrder.entries

			val newIndex = Math.floorMod(current.ordinal + offset, entires.size)

			extractorData.metaData.sortingOrder = entires[newIndex]

			ExtractorManager.saveExtractor(block.world, block.x, block.y, block.z, extractorData)

			event.view.setTitle(getSlotOverlay())
		}
	}
}
