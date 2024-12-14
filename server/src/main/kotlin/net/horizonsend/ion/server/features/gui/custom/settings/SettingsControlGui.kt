package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.settings.commands.ControlSettingsCommand
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class SettingsControlGui(val player: Player) : AbstractBackgroundPagedGui {

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
    }


    override var currentWindow: Window? = null

    private val buttonsList = listOf(
        DcOverrideButton(),
        DcSpeedModifierButton()
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

        val enabledSettings = listOf(
            PlayerCache[player.uniqueId].useAlternateDCCruise,
            PlayerCache[player.uniqueId].dcSpeedModifier
        )

        // create a new GuiText builder
        val header = "Control Settings"
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
                component = if (enabledSettings[buttonIndex] is Boolean){
                    if (enabledSettings[buttonIndex] as Boolean) text("ENABLED", GREEN) else text("DISABLED", RED)
                } else when (buttonIndex) {
                    1 -> text(PlayerCache[player.uniqueId].dcSpeedModifier)
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

    private inner class DcOverrideButton : GuiItems.AbstractButtonItem(
        text("DC Overrides Cruise").itemName,
		GuiItem.GUNSHIP.makeItem()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val alternateDcCruise = PlayerCache[player.uniqueId].useAlternateDCCruise
            ControlSettingsCommand.onUseAlternateDCCruise(player, !alternateDcCruise)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class DcSpeedModifierButton : GuiItems.AbstractButtonItem(
        text("DC Speed Modifier").itemName,
        GuiItem.GUNSHIP.makeItem()
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            ControlSettingsCommand.onChangeDcModifier(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }
}
