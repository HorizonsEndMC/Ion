package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsSidebarGui.ReturnToMainMenuButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import kotlin.math.min

object SettingsHudGui : AbstractBackgroundPagedGui {

    private const val SETTINGS_PER_PAGE = 5

    private val BUTTONS_LIST = listOf(
        PlanetSettingsButton()
    )

    override fun createGui(): PagedGui<Item> {
        val gui = PagedGui.items()

        gui.setStructure(
            "x . . . . . . . .",
            "x . . . . . . . .",
            "x . . . . . . . .",
            "x . . . . . . . .",
            "x . . . . . . . .",
            "< . . . v . . . >"
        )

        gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_VERTICAL)
            .addIngredient('<', GuiItems.LeftItem())
            .addIngredient('>', GuiItems.RightItem())
            .addIngredient('v', ReturnToMainMenuButton())
            .setContent(BUTTONS_LIST)

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {

        // create a new GuiText builder
        val header = "Planet Settings"
        val guiText = GuiText(header)
        guiText.addBackground()

        // get the index of the first setting to display for this page
        val startIndex = currentPage * SETTINGS_PER_PAGE

        for (buttonIndex in startIndex until min(startIndex + SETTINGS_PER_PAGE, BUTTONS_LIST.size)) {

            val title = BUTTONS_LIST[buttonIndex].text
            val line = (buttonIndex - startIndex) * 2

            // setting title
            guiText.add(
                component = title,
                line = line,
                horizontalShift = 21,
                verticalShift = 5
            )
        }

        return guiText.build()
    }

    class PlanetSettingsButton : GuiItems.AbstractButtonItem(
        text("Planet Settings").decoration(ITALIC, false),
        CustomItems.CHANDRA.constructItemStack()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        }
    }
}