package net.horizonsend.ion.server.features.gui.custom.item

import net.horizonsend.ion.server.features.custom.items.type.PersonalTransporterManager
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import kotlin.math.ceil
import kotlin.math.min

class PersonalTransporterGui(viewer: Player) : AbstractBackgroundPagedGui(viewer) {

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
    }

    // cache current player list
    private val playerList = mutableListOf<Player>()

	private var currentPage: Int = 0

    override fun createGui(): PagedGui<Item> {
        val gui = PagedGui.items()

        gui.setStructure(
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "< . . . . . . . >"
        )

        gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', GuiItems.PageLeftItem())
            .addIngredient('>', GuiItems.PageRightItem())
			.addPageChangeHandler { _, new ->
				currentPage = new
				refreshTitle()
			}

        // populate player list cache
        playerList.addAll(Bukkit.getOnlinePlayers())

        for (otherPlayer in playerList) {
            val button = GuiItems.PlayerHeadItem(otherPlayer.uniqueId, otherPlayer.name) {
                PersonalTransporterManager.addTpRequest(viewer, otherPlayer)
            }
            gui.addContent(button)

            repeat(8) { gui.addContent(GuiItems.BlankButton(button)) }
        }

        return gui.build()
    }

	override fun buildTitle(): Component {
        // create a new GuiText builder
        val header = "Personal Transporter"
        val guiText = GuiText(header)
        guiText.addBackground()

        // get the index of the first setting to display for this page
        val startIndex = currentPage * SETTINGS_PER_PAGE

        for (buttonIndex in startIndex until min(startIndex + SETTINGS_PER_PAGE, playerList.size)) {

            val title = text("Request TP to " + playerList[buttonIndex].name)
            val line = (buttonIndex - startIndex) * 2

            // player username
            guiText.add(
                component = title,
                line = line,
                horizontalShift = 21
            )

            // location
            guiText.add(
                component = text("in: " + playerList[buttonIndex].world.name),
                line = line + 1,
                horizontalShift = 21
            )
        }

        // page number
        val pageNumberString =
            "${currentPage + 1} / ${ceil((playerList.size.toDouble() / SETTINGS_PER_PAGE)).toInt()}"
        guiText.add(
            text(pageNumberString),
            line = 10,
            GuiText.TextAlignment.CENTER,
            verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
        )

        return guiText.build()
    }
}
