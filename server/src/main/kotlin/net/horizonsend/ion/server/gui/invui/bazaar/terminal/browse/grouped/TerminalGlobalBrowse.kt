package net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse.grouped

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs.openTerminalPurchaseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.browse.BazaarGlobalBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse.IndividualBrowseGui
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse.TerminalCitySelection
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class TerminalGlobalBrowse(viewer: Player, val entity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity) : BazaarGlobalBrowseMenu(viewer) {
	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . c g . . . i",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s . . . S . >"
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)
			.addIngredient('i', infoButton)

			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())

			.addIngredient('s', searchButton)
			.addIngredient('S', sortButton)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)
			.handlePageChange()

			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component = GuiText("Browsing Global")
		.setSlotOverlay(
			"# # # # # # # # #",
			". . . . . . . . .",
			". . . . . . . . .",
			". . . . . . . . .",
			"# # # # # # # # #"
		)
		.build()

	override fun goToCitySelection(viewer: Player) {
		TerminalCitySelection(viewer, entity).openGui(parentWindow)
	}

	override fun goToGlobalBrowse(viewer: Player) {
		TerminalGlobalBrowse(viewer, entity).openGui(parentWindow)
	}

	override fun onClickDisplayedItem(entry: Map.Entry<String, List<BazaarItem>>, clickedFrom: CommonGuiWrapper) {
		IndividualBrowseGui(
			viewer = viewer,
			contextName = Component.text("Global"),
			isGlobalBrowse = true,
			itemString = entry.key,
			parentBson = bson,
			purchaseHandler = { item: BazaarItem -> openTerminalPurchaseMenu(viewer, item, null, entity) { openGui() } },
			openGlobalBrowse = ::goToGlobalBrowse,
			openCityBrowse = ::goToCitySelection,
		).openGui(this)
	}
}
