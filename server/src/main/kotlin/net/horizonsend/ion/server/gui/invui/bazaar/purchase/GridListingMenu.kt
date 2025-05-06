package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.kyori.adventure.text.Component
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
                "x . . . . S s . i",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "# # # # # # # # #",
                "< . . . . . . . >",
            )
            .addIngredient('x', backButton)
            .addIngredient('s', sortButton)
            .addIngredient('S', searchButton)
            .addIngredient('i', infoButton)
            .addIngredient('<', GuiItems.PageLeftItem())
            .addIngredient('>', GuiItems.PageRightItem())
            .addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, new ->
                pageNumber = new
                refreshWindowText()
            }
            .setContent(guiItems)
            .build()

        return Window
            .single()
            .setGui(gui)
            .setViewer(viewer)
            .setTitle(buildGuiText())
            .build()
    }

    override fun buildGuiText(): Component {
        val guiText =  GuiText("Your Bazaar Sale Listings", guiWidth = DEFAULT_GUI_WIDTH - 20)
            .addBackground()

        val pageNumber = addPageNumber(LISTINGS_PER_PAGE)
        return ofChildren(guiText.build(), pageNumber)
    }
}