package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import kotlin.math.ceil
import kotlin.math.min

object SettingsSidebarGui : AbstractBackgroundPagedGui {
    private const val SETTINGS_PER_PAGE = 5
    private const val PAGE_NUMBER_VERTICAL_SHIFT = 4

    private val BUTTONS_LIST = listOf(
        StarshipsSettingsButton(),
        ContactsSettingsButton(),
        RouteSettingsButton()
    )

    override fun createGui(): PagedGui<Item> {
        val gui = PagedGui.items()

        gui.setStructure(
            "x . . . . . . . .",
            "x . . . . . . . .",
            "x . . . . . . . .",
            "x . . . . . . . .",
            "x . . . . . . . .",
            "< v . . . . . . >"
        )

        gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_VERTICAL)
            .addIngredient('<', GuiItems.LeftItem())
            .addIngredient('>', GuiItems.RightItem())
            .addIngredient('v', SettingsMainMenuGui.ReturnToMainMenuButton())
            .setContent(BUTTONS_LIST)

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {

        // create a new GuiText builder
        val header = "Sidebar Settings"
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

        // page number
        val pageNumberString =
            "${currentPage + 1} / ${ceil((BUTTONS_LIST.size.toDouble() / SETTINGS_PER_PAGE)).toInt()}"
        guiText.add(
            text(pageNumberString),
            line = 10,
            GuiText.TextAlignment.CENTER,
            verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
        )

        return guiText.build()
    }

    class StarshipsSettingsButton : GuiItems.AbstractButtonItem(
        text("Starships Settings").decoration(ITALIC, false),
        CustomItems.CHANDRA.constructItemStack()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarStarshipsGui.open(player)
        }
    }

    class ContactsSettingsButton : GuiItems.AbstractButtonItem(
        text("Contacts Settings").decoration(ITALIC, false),
        CustomItems.CHANDRA.constructItemStack()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsGui.open(player)
        }
    }

    class RouteSettingsButton : GuiItems.AbstractButtonItem(
        text("Route Settings").decoration(ITALIC, false),
        CustomItems.CHANDRA.constructItemStack()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarRouteGui.open(player)
        }
    }

    class ReturnToSidebarButton : GuiItems.AbstractButtonItem(
        text("Return to Sidebar Settings").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
            it.setCustomModelData(UI_DOWN)
            it.displayName(text("Return to Sidebar Settings").decoration(ITALIC, false))
        }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarGui.open(player)
        }

        companion object {
            private const val UI_DOWN = 104
        }
    }
}