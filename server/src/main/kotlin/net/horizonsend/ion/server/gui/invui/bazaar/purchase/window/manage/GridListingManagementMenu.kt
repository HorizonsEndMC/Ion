package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.BAZAAR_LISTING_HEADER_ICON
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_BLUE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class GridListingManagementMenu(viewer: Player) : AbstractListingManagementMenu(viewer) {
	override val listingsPerPage: Int = 36

    override fun buildWindow(): Window {
        val gui = PagedGui.items()
            .setStructure(
                "x . c . S . . l i",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "< . . . . . f s >",
            )
            .addIngredient('x', parentOrBackButton())
            .addIngredient('f', filterButton)
			.addIngredient('s', sortButton)
            .addIngredient('S', searchButton)
            .addIngredient('l', listViewButton)
            .addIngredient('i', infoButton)
            .addIngredient('c', collectButton)
            .addIngredient('<', GuiItems.PageLeftItem())
            .addIngredient('>', GuiItems.PageRightItem())
            .addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, new ->
                pageNumber = new
            }
            .setContent(items)
            .build()

        return normalWindow(gui)
    }

	override fun buildTitle(): Component {
        val guiText =  GuiText("", guiWidth = DEFAULT_GUI_WIDTH - 20)
			.addBorder(GuiBorder.regular(
				color = HE_DARK_BLUE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_LISTING_HEADER_ICON, 48, HE_DARK_BLUE),
				leftText = text("Managing"),
				rightText = text("Sell Orders")
			))
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)

        return withPageNumber(guiText)
    }

    private val listViewButton = GuiItem.LIST_VIEW.makeItem(text("Switch to List view")).makeGuiButton { _, _ ->
		viewer.setSetting(PlayerSettings::listingManageDefaultListView, true)
		BazaarGUIs.openListingManageListMenu(viewer, parentWindow)
	}
}
