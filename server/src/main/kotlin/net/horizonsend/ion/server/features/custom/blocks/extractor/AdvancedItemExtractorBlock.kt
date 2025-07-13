package net.horizonsend.ion.server.features.custom.blocks.extractor

import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI.Companion.setTitle
import net.horizonsend.ion.server.features.transport.items.SortingOrder
import net.horizonsend.ion.server.features.transport.manager.extractors.ExtractorManager
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.gui.invui.InvUIWrapper
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.MetaDataContainer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.TileState
import org.bukkit.block.data.type.Vault
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

object AdvancedItemExtractorBlock : CustomExtractorBlock<ItemExtractorData>(
	"ADVANCED_ITEM_EXTRACTOR",
	blockData = Material.VAULT.createBlockData { t ->
		t as Vault
		t.facing = BlockFace.SOUTH
		t.isOminous = false
		t.vaultState = Vault.State.INACTIVE
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

	override fun createExtractorData(pos: BlockKey, metaData: ExtractorMetaData): ItemExtractorData {
		return ItemExtractorData(pos, metaData as ItemExtractorData.ItemExtractorMetaData)
	}

	override fun openGUI(player: Player, block: Block, extractorData: ItemExtractorData) {
		AdvancedItemExtractorGUI(player, block, extractorData).openGui()
	}

	override fun placeCallback(placedItem: ItemStack, block: Block) {
		val extractorData = placedItem.persistentDataContainer.get(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer) ?: return

		val state = block.state
		if (state !is TileState) return

		state.persistentDataContainer.set(NamespacedKeys.COMPLEX_EXTRACTORS, MetaDataContainer, extractorData)
		state.update()
	}

	class AdvancedItemExtractorGUI(override val viewer: Player, val block: Block, private val extractorData: ItemExtractorData) : InvUIWrapper {
		override fun buildWindow(): Window {
			val gui = Gui.normal()
				.setStructure(
					"u u u u u u u u u",
					". . . . . . . . .",
					"d d d d d d d d d"
				)
				.addIngredient('u', getTraverseButton(1))
				.addIngredient('d', getTraverseButton(-1))

			return Window
				.single()
				.setGui(gui)
				.setTitle(AdventureComponentWrapper(getSlotOverlay()))
				.build(viewer)
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
				verticalShift = +4
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
				verticalShift = -4
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
