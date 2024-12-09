package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.utils.text.SPACE_MAIN_HYPERSPACE_ROUTES_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_MINOR_HYPERSPACE_ROUTES_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_SCREEN_CHARACTER
import net.horizonsend.ion.common.utils.text.SPACE_STARRY_BACKGROUND_CHARACTER
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class NavigationGalacticMapGui(val player: Player) {

    private var currentWindow: Window? = null
    private val gui = Gui.empty(9, 6)

    companion object {
        private const val MENU_ROW = 5
    }

    private fun createGui(): Gui {
        gui.setItem(1, 4, GuiItems.CustomControlItem("Asteri", GuiItem.ASTERI_2))
        gui.setItem(3, 4, GuiItems.CustomControlItem("Regulus", GuiItem.REGULUS_2))
        gui.setItem(5, 4, GuiItems.CustomControlItem("Ilios", GuiItem.ILIOS_2))
        gui.setItem(1, 2, GuiItems.CustomControlItem("Sirius", GuiItem.SIRIUS_2))
        gui.setItem(4, 2, GuiItems.CustomControlItem("Horizon", GuiItem.HORIZON_2))
        gui.setItem(3, 0, GuiItems.CustomControlItem("The Trench", GuiItem.HORIZON_2))
        gui.setItem(7, 2, GuiItems.CustomControlItem("AU-0821", GuiItem.HORIZON_2))

        return gui
    }

    private fun createText(): Component {
        val header = "Galactic Map"
        val guiText = GuiText(header)

        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_SCREEN_CHARACTER))
        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_STARRY_BACKGROUND_CHARACTER, backgroundWidth = 143))
        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_MINOR_HYPERSPACE_ROUTES_CHARACTER, backgroundWidth = 122))
        guiText.addBackground(GuiText.GuiBackground(backgroundChar = SPACE_MAIN_HYPERSPACE_ROUTES_CHARACTER, backgroundWidth = 136))

        return guiText.build()
    }

    fun openMainWindow() {
        val gui = createGui()

        val window = Window.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle(AdventureComponentWrapper(createText()))
            .build()

        currentWindow = window.apply { open() }
    }
}