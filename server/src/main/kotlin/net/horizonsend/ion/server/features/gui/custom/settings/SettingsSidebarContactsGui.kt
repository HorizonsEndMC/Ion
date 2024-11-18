package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.sidebar.command.SidebarContactsCommand
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
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
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class SettingsSidebarContactsGui(val player: Player) : AbstractBackgroundPagedGui {

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
    }

    override var currentWindow: Window? = null

    private val buttonsList = listOf(
        EnableButton(),
        ContactsDistanceButton(),
        ContactsMaxNameLengthButton(),
        ContactsSortOrderButton(),
        ContactsColoringButton(),
        StarshipsButton(),
        LastStarshipsButton(),
        PlanetsButton(),
        StarsButton(),
        BeaconsButton(),
        StationsButton(),
        BookmarksButton(),
        RelationAiButton(),
        RelationNoneButton(),
        RelationEnemyButton(),
        RelationUnfriendlyButton(),
        RelationNeutralButton(),
        RelationFriendlyButton(),
        RelationAllyButton(),
        RelationNationButton(),
        RelationAiStationButton(),
        RelationNoneStationButton(),
        RelationEnemyStationButton(),
        RelationUnfriendlyStationButton(),
        RelationNeutralStationButton(),
        RelationFriendlyStationButton(),
        RelationAllyStationButton(),
        RelationNationStationButton(),
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
            .addIngredient('v', SettingsSidebarGui(player).ReturnToSidebarButton())

        for (button in buttonsList) {
            gui.addContent(button)

            for (i in 1..8) {
                gui.addContent(GuiItems.BlankButton(button))
            }
        }

        return gui.build()
    }

    override fun createText(player: Player, currentPage: Int): Component {
        val enabledSettings = listOf(
            PlayerCache[player.uniqueId].contactsEnabled,
            PlayerCache[player.uniqueId].contactsDistance,
            PlayerCache[player.uniqueId].contactsMaxNameLength,
            PlayerCache[player.uniqueId].contactsSort,
            PlayerCache[player.uniqueId].contactsColoring,
            PlayerCache[player.uniqueId].contactsStarships,
            PlayerCache[player.uniqueId].lastStarshipEnabled,
            PlayerCache[player.uniqueId].planetsEnabled,
            PlayerCache[player.uniqueId].starsEnabled,
            PlayerCache[player.uniqueId].beaconsEnabled,
            PlayerCache[player.uniqueId].stationsEnabled,
            PlayerCache[player.uniqueId].bookmarksEnabled,
            PlayerCache[player.uniqueId].relationAiEnabled,
            PlayerCache[player.uniqueId].relationNoneEnabled,
            PlayerCache[player.uniqueId].relationEnemyEnabled,
            PlayerCache[player.uniqueId].relationUnfriendlyEnabled,
            PlayerCache[player.uniqueId].relationNeutralEnabled,
            PlayerCache[player.uniqueId].relationFriendlyEnabled,
            PlayerCache[player.uniqueId].relationAllyEnabled,
            PlayerCache[player.uniqueId].relationNationEnabled,
            PlayerCache[player.uniqueId].relationAiStationEnabled,
            PlayerCache[player.uniqueId].relationNoneStationEnabled,
            PlayerCache[player.uniqueId].relationEnemyStationEnabled,
            PlayerCache[player.uniqueId].relationUnfriendlyStationEnabled,
            PlayerCache[player.uniqueId].relationNeutralStationEnabled,
            PlayerCache[player.uniqueId].relationFriendlyStationEnabled,
            PlayerCache[player.uniqueId].relationAllyStationEnabled,
            PlayerCache[player.uniqueId].relationNationStationEnabled,
        )

        // create a new GuiText builder
        val header = "Sidebar Contacts Settings"
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
                    3 -> text(ContactsSidebar.ContactsSorting.entries[PlayerCache[player.uniqueId].contactsSort].toString())
                    4 -> text(ContactsSidebar.ContactsColoring.entries[PlayerCache[player.uniqueId].contactsColoring].toString())
                    else -> Component.empty()
                },
                line = line + 1,
                horizontalShift = 21
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

    private inner class EnableButton : GuiItems.AbstractButtonItem(
        text("Enable Contacts Info").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val contactsEnabled = PlayerCache[player.uniqueId].contactsEnabled

            if (contactsEnabled) SidebarContactsCommand.onDisableContacts(player)
            else SidebarContactsCommand.onEnableContacts(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class ContactsDistanceButton : GuiItems.AbstractButtonItem(
        text("Change Contacts Range").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.ROUTE_SEGMENT.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsRangeGui(player).openMainWindow()
        }
    }

    private inner class ContactsMaxNameLengthButton : GuiItems.AbstractButtonItem(
        text("Change Max Name Length").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SettingsSidebarContactsMaxNameLengthGui(player).openMainWindow()
        }
    }

    private inner class ContactsSortOrderButton : GuiItems.AbstractButtonItem(
        text("Change Sort Order").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onChangeContactsSortOrder(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class ContactsColoringButton : GuiItems.AbstractButtonItem(
        text("Change Coloring").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.LIST.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onChangeContactsColoring(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class StarshipsButton : GuiItems.AbstractButtonItem(
        text("Enable Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleStarship(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class LastStarshipsButton : GuiItems.AbstractButtonItem(
        text("Enable Last Starship").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GENERIC_STARSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleLastStarship(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class PlanetsButton : GuiItems.AbstractButtonItem(
        text("Enable Planets").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.PLANET.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onTogglePlanets(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class StarsButton : GuiItems.AbstractButtonItem(
        text("Enable Stars").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STAR.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleStars(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class BeaconsButton : GuiItems.AbstractButtonItem(
        text("Enable Beacons").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.BEACON.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleBeacons(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class StationsButton : GuiItems.AbstractButtonItem(
        text("Enable Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleStations(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class BookmarksButton : GuiItems.AbstractButtonItem(
        text("Enable Bookmarks").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.BOOKMARK.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleBookmarks(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationAiButton : GuiItems.AbstractButtonItem(
        text("Enable AI Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleAi(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationNoneButton : GuiItems.AbstractButtonItem(
        text("Enable No Relation Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleNone(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationEnemyButton : GuiItems.AbstractButtonItem(
        text("Enable Enemy Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleEnemy(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationUnfriendlyButton : GuiItems.AbstractButtonItem(
        text("Enable Unfriendly Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleUnfriendly(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationNeutralButton : GuiItems.AbstractButtonItem(
        text("Enable Neutral Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleNeutral(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationFriendlyButton : GuiItems.AbstractButtonItem(
        text("Enable Friendly Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleFriendly(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationAllyButton : GuiItems.AbstractButtonItem(
        text("Enable Ally Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleAlly(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationNationButton : GuiItems.AbstractButtonItem(
        text("Enable Nation Starships").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.GUNSHIP.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleNation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationAiStationButton : GuiItems.AbstractButtonItem(
        text("Enable AI Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleAiStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationNoneStationButton : GuiItems.AbstractButtonItem(
        text("Enable No Relation Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleNoneStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationEnemyStationButton : GuiItems.AbstractButtonItem(
        text("Enable Enemy Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleEnemyStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationUnfriendlyStationButton : GuiItems.AbstractButtonItem(
        text("Enable Unfriendly Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleUnfriendlyStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationNeutralStationButton : GuiItems.AbstractButtonItem(
        text("Enable Neutral Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleNeutralStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationFriendlyStationButton : GuiItems.AbstractButtonItem(
        text("Enable Friendly Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleFriendlyStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationAllyStationButton : GuiItems.AbstractButtonItem(
        text("Enable Ally Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleAllyStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class RelationNationStationButton : GuiItems.AbstractButtonItem(
        text("Enable Nation Stations").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.STATION.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SidebarContactsCommand.onToggleNationStation(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    inner class ReturnToSidebarContactsButton : ControlItem<Gui>() {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            openMainWindow()
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
