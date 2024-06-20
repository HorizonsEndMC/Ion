package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.commands.HudCommand
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
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
import kotlin.math.ceil
import kotlin.math.min

object SettingsHudPlanetsGui : AbstractBackgroundPagedGui {

    private const val SETTINGS_PER_PAGE = 5
    private const val PAGE_NUMBER_VERTICAL_SHIFT = 4

    private val BUTTONS_LIST = listOf(
        ImageButton(),
        SelectorButton()
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
            .addIngredient('v', SettingsHudGui.ReturnToHudButton())
            .setContent(BUTTONS_LIST)

        return gui.build()

    }

    override fun createText(player: Player, currentPage: Int): Component {

        val enabledSettings = listOf(
            PlayerCache[player.uniqueId].hudPlanetsImage,
            PlayerCache[player.uniqueId].hudPlanetsSelector
        )

        // create a new GuiText builder
        val header = "HUD Contacts Settings"
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
                component = if (enabledSettings[buttonIndex]) text("ENABLED", GREEN) else text("DISABLED", RED),
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

    class ImageButton : GuiItems.AbstractButtonItem(
        text("Toggle Planet Visibility").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.PLANET.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onTogglePlanetsImage(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    class SelectorButton : GuiItems.AbstractButtonItem(
        text("Toggle Planet Selector").decoration(TextDecoration.ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.PLANET.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            HudCommand.onTogglePlanetsSelector(player, null)

            windows.find { it.viewer == player }?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }
}