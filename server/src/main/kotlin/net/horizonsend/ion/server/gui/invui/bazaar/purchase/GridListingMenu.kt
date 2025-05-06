package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class GridListingMenu(viewer: Player, backButtonHandler: () -> Unit = {}) : AbstractListingMenu(viewer, backButtonHandler) {
    companion object {
        private const val LISTINGS_PER_PAGE = 36
    }

    override fun buildWindow(): Window {
        val guiItems = generateItemListings()

        val gui = PagedGui.items()
            .setStructure(
                "x . . . f S s l i",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "< . . . . . . . >",
            )
            .addIngredient('x', backButton)
            .addIngredient('f', filterButton)
            .addIngredient('S', searchButton)
            .addIngredient('s', sortButton)
            .addIngredient('l', listViewButton)
            .addIngredient('i', infoButton)
            .addIngredient('<', GuiItems.PageLeftItem())
            .addIngredient('>', GuiItems.PageRightItem())
            .addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, new ->
                pageNumber = new
            }
            .setContent(guiItems)
            .build()

        return normalWindow(gui)
    }

	override fun buildTitle(): Component {
        val guiText =  GuiText("Your Bazaar Sale Listings", guiWidth = DEFAULT_GUI_WIDTH - 20)
            .addBackground()

        val pageNumber = addPageNumber(LISTINGS_PER_PAGE)
        return ofChildren(guiText.build(), pageNumber)
    }

    private val listViewButton = GuiItem.LIST_VIEW.makeItem(text("List view")).makeGuiButton { _, _ -> ListListingMenu(viewer, { this.openGui() }).openGui() }
}
