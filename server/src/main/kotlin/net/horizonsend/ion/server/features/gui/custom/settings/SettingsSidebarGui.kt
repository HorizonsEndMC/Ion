package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class SettingsSidebarGui(val player: Player) : AbstractBackgroundPagedGui {

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
    }

    override var currentWindow: Window? = null

    private val buttonsList = listOf(
        CombatTimerSettingsButton(),
        StarshipsSettingsButton(),
        ContactsSettingsButton(),
        RouteSettingsButton()
    )

    override fun createGui(): PagedGui<Item> {
        val gui = PagedGui.items()

        gui.setStructure(
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "< v . . . . . . >"
        )

        gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', GuiItems.PageLeftItem())
            .addIngredient('>', GuiItems.PageRightItem())
            .addIngredient('v', SettingsMainMenuGui(player).ReturnToMainMenuButton())

        for (button in buttonsList) {
            gui.addContent(button)

            for (i in 1..8) {
                gui.addContent(GuiItems.BlankButton(button))
            }
        }

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {

        // create a new GuiText builder
        val header = "Sidebar Settings"
        val guiText = GuiText(header)
        guiText.addBackground()

        // get the index of the first setting to display for this page
        val startIndex = currentPage * SETTINGS_PER_PAGE

        for (buttonIndex in startIndex until min(startIndex + SETTINGS_PER_PAGE, buttonsList.size)) {

            val title = buttonsList[buttonIndex].text
            val line = (buttonIndex - startIndex) * 2

            // setting title
            guiText.add(
                component = title,
                line = line,
                horizontalShift = 21,
                verticalShift = 5
            )
        }

        // page number
        val pageNumberString =
            "${currentPage + 1} / ${ceil((buttonsList.size.toDouble() / SETTINGS_PER_PAGE)).toInt()}"
        guiText.add(
            text(pageNumberString),
            line = 10,
            GuiText.TextAlignment.CENTER,
            verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
        )

        return guiText.build()
    }

    fun openMainWindow() {
        currentWindow = open(player).apply { open() }
    }
    private inner class CombatTimerSettingsButton : GuiItems.AbstractButtonItem(
        text("Combat Timer Settings").decoration(ITALIC, false),
        GuiItem.COMPASS_NEEDLE.makeItem()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarCombatTimerGui(player).openMainWindow()
        }
    }

    private inner class StarshipsSettingsButton : GuiItems.AbstractButtonItem(
        text("Starships Settings").decoration(ITALIC, false),
        GuiItem.GENERIC_STARSHIP.makeItem()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarStarshipsGui(player).openMainWindow()
        }
    }

    private inner class ContactsSettingsButton : GuiItems.AbstractButtonItem(
        text("Contacts Settings").decoration(ITALIC, false),
        GuiItem.LIST.makeItem()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsGui(player).openMainWindow()
        }
    }

    private inner class RouteSettingsButton : GuiItems.AbstractButtonItem(
        text("Route Settings").decoration(ITALIC, false),
        GuiItem.ROUTE_SEGMENT.makeItem()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarRouteGui(player).openMainWindow()
        }
    }

    inner class ReturnToSidebarButton : GuiItems.AbstractButtonItem(
        text("Return to Sidebar Settings").decoration(ITALIC, false),
        GuiItem.DOWN.makeItem().updateDisplayName(text("Return to Sidebar Settings"))
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarGui(player).openMainWindow()
        }
    }
}
