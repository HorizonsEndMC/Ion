package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.commands.HudCommand
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItem.Companion.applyGuiModel
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class SettingsHudIconsGui(val player: Player) : AbstractBackgroundPagedGui {

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
    }

    override var currentWindow: Window? = null

    private val buttonsList = listOf(
        SelectorButton(),
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
            .addIngredient('<', GuiItems.PageLeftItem())
            .addIngredient('>', GuiItems.PageRightItem())
            .addIngredient('v', SettingsHudGui(player).ReturnToHudButton())

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
            PlayerCache[player.uniqueId].hudPlanetsSelector,
            PlayerCache[player.uniqueId].hudPlanetsImage,
            PlayerCache[player.uniqueId].hudIconStars,
            PlayerCache[player.uniqueId].hudIconBeacons,
            PlayerCache[player.uniqueId].hudIconStations,
            PlayerCache[player.uniqueId].hudIconBookmarks
        )

        // create a new GuiText builder
        val header = "HUD Icon Settings"
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
                component = if (enabledSettings[buttonIndex]) text("ENABLED", GREEN) else text("DISABLED", RED),
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

    private inner class SelectorButton : GuiItems.AbstractButtonItem(
        text("Toggle Planet Selector").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(GuiItem.COMPASS_NEEDLE)
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onToggleHudSelector(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class PlanetsButton : GuiItems.AbstractButtonItem(
        text("Toggle Planet Visibility").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(GuiItem.PLANET)
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onToggleHudPlanets(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class StarsButton : GuiItems.AbstractButtonItem(
        text("Toggle Star Visibility").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(GuiItem.STAR)
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onToggleHudStars(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class BeaconsButton : GuiItems.AbstractButtonItem(
        text("Toggle Beacon Visibility").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(GuiItem.BEACON)
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onToggleHudBeacons(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class StationsButton : GuiItems.AbstractButtonItem(
        text("Toggle Station Visibility").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(GuiItem.STATION)
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onToggleHudStations(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class BookmarksButton : GuiItems.AbstractButtonItem(
        text("Toggle Bookmark Visibility").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(GuiItem.BOOKMARK)
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onToggleHudBookmarks(player, null)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }
}
