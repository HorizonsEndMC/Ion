package net.horizonsend.ion.server.gui.invui.bazaar.orders.window.manage

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class GridOrderManagementWindow(viewer: Player) : AbstractOrderManagementMenu(viewer) {
	override val listingsPerPage: Int = 36

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"b . . . p . . g i",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"l . . . . . f s r",
			)
			.addIngredient('f', filterButton)
			.addIngredient('s', sortButton)
			.addIngredient('b', parentOrBackButton())
			.addIngredient('g', switchLayoutButton)
			.addIngredient('i', infoButton)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('p', createBuyOrderMenu)
			.setContent(items)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("Managing Your Bazaar Orders")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)

		return withPageNumber(text)
	}

	override val switchLayoutButton: Item = GuiItem.LIST_VIEW.makeItem(Component.text("Switch to List Layout")).makeGuiButton { _, _ ->
		viewer.setSetting(PlayerSettings::orderManageDefaultListView, true)
		BazaarGUIs.openBuyOrderManageListMenu(viewer, parentWindow)
	}
}
