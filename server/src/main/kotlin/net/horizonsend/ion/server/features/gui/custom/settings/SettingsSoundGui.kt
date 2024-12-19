package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.settings.commands.SoundSettingsCommand
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
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class SettingsSoundGui(val player: Player) : AbstractBackgroundPagedGui {

    companion object {
        private const val SETTINGS_PER_PAGE = 5
        private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
    }


    override var currentWindow: Window? = null

    private val buttonsList = listOf(
        EnableButton(),
        CruiseIndicatorSoundButton(),
		HitmarkerOnHullSoundButton(),
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
            .addIngredient('<', GuiItems.LeftPageItem())
            .addIngredient('>', GuiItems.RightPageItem())
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
            PlayerCache[player.uniqueId].enableAdditionalSounds,
            PlayerCache[player.uniqueId].soundCruiseIndicator,
			PlayerCache[player.uniqueId].hitmarkerOnHull,
        )

        // create a new GuiText builder
        val header = "Sound Settings"
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
                    1 -> text(SoundSettingsCommand.CruiseIndicatorSounds.entries[PlayerCache[player.uniqueId].soundCruiseIndicator].toString())
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

    private inner class EnableButton: GuiItems.AbstractButtonItem(
        text("Enable Additional Sounds").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.SOUND.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val enableAdditionalSounds = PlayerCache[player.uniqueId].enableAdditionalSounds
            SoundSettingsCommand.onToggleEnableAdditionalSounds(player, !enableAdditionalSounds)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

    private inner class CruiseIndicatorSoundButton: GuiItems.AbstractButtonItem(
        text("Cruise Indicator Sound").decoration(ITALIC, false),
        ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.SOUND.customModelData) }
    ) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SoundSettingsCommand.onChangeCruiseIndicatorSound(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
    }

	private inner class HitmarkerOnHullSoundButton: GuiItems.AbstractButtonItem(
		text("Hitmarker On Hull").decoration(ITALIC, false),
		ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(GuiItem.SOUND.customModelData) }
	) {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            SoundSettingsCommand.onChangeCruiseIndicatorSound(player)

            currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
        }
	}
}
