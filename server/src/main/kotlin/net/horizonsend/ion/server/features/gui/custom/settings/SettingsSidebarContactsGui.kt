package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.sidebar.command.SidebarContactsCommand
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import kotlin.math.ceil
import kotlin.math.min

object SettingsSidebarContactsGui : AbstractBackgroundPagedGui {

    private const val SETTINGS_PER_PAGE = 5
    private const val PAGE_NUMBER_VERTICAL_SHIFT = 4

    private val BUTTONS_LIST = listOf(
        EnableButton(),
        ContactsDistanceButton(),
        ContactsMaxNameLengthButton(),
        StarshipsButton(),
        LastStarshipsButton(),
        PlanetsButton(),
        StarsButton(),
        BeaconsButton(),
        StationsButton(),
        BookmarksButton()
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
            .addIngredient('<', GuiItems.LeftItem())
            .addIngredient('>', GuiItems.RightItem())
            .addIngredient('v', SettingsSidebarGui.ReturnToSidebarButton())

        for (button in BUTTONS_LIST) {
            gui.addContent(button)

            for (i in 1..8) {
                gui.addContent(GuiItems.BlankItem(button))
            }
        }

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {

        val enabledSettings = listOf(
            PlayerCache[player.uniqueId].contactsEnabled,
            PlayerCache[player.uniqueId].contactsDistance,
            PlayerCache[player.uniqueId].contactsMaxNameLength,
            PlayerCache[player.uniqueId].contactsStarships,
            PlayerCache[player.uniqueId].lastStarshipEnabled,
            PlayerCache[player.uniqueId].planetsEnabled,
            PlayerCache[player.uniqueId].starsEnabled,
            PlayerCache[player.uniqueId].beaconsEnabled,
            PlayerCache[player.uniqueId].stationsEnabled,
            PlayerCache[player.uniqueId].bookmarksEnabled
        )

        // create a new GuiText builder
        val header = "Sidebar Contacts Settings"
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
                horizontalShift = 21
            )

            // setting description
            guiText.add(
                component = if (enabledSettings[buttonIndex] is Boolean) {
                    if (enabledSettings[buttonIndex] as Boolean) text("ENABLED", GREEN) else text("DISABLED", RED)
                } else when (buttonIndex) {
                    // TODO: Find a better way to handle Boolean settings vs. Int settings
                    // Index values correlating to the Int setting
                    1 -> text(PlayerCache[player.uniqueId].contactsDistance)
                    2 -> text(PlayerCache[player.uniqueId].contactsMaxNameLength)
                    else -> Component.empty()
                },
                line = line + 1,
                horizontalShift = 21
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

    private class EnableButton : GuiItems.AbstractButtonItem(
        text("Enable Contacts Info").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val contactsEnabled = PlayerCache[player.uniqueId].contactsEnabled

            if (contactsEnabled) SidebarContactsCommand.onDisableContacts(player)
            else SidebarContactsCommand.onEnableContacts(player)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class ContactsDistanceButton : GuiItems.AbstractButtonItem(
        text("Change Contacts Range").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.ROUTE_SEGMENT.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsRangeGui.open(player)
        }
    }

    private class ContactsMaxNameLengthButton : GuiItems.AbstractButtonItem(
        text("Change Max Name Length").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsMaxNameLengthGui.open(player)
        }
    }

    private class StarshipsButton : GuiItems.AbstractButtonItem(
        text("Enable Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleStarship(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class LastStarshipsButton : GuiItems.AbstractButtonItem(
        text("Enable Last Starship").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GENERIC_STARSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleLastStarship(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class PlanetsButton : GuiItems.AbstractButtonItem(
        text("Enable Planets").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.PLANET.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onTogglePlanets(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class StarsButton : GuiItems.AbstractButtonItem(
        text("Enable Stars").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STAR.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleStars(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class BeaconsButton : GuiItems.AbstractButtonItem(
        text("Enable Beacons").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.BEACON.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleBeacons(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class StationsButton : GuiItems.AbstractButtonItem(
        text("Enable Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleStations(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private class BookmarksButton : GuiItems.AbstractButtonItem(
        text("Enable Bookmarks").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.BOOKMARK.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleBookmarks(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    class ReturnToSidebarContactsButton : ControlItem<Gui>() {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsGui.open(player)
        }

        override fun getItemProvider(gui: Gui): ItemProvider {
            val builder = ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.DOWN.customModelData)
                it.displayName(text("Return to Sidebar Contacts Settings").decoration(ITALIC, false))
            })
            return builder
        }
    }
}