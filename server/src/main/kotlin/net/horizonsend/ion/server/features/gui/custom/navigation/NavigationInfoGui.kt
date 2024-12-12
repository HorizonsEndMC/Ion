package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.GUI_HEADER_MARGIN
import net.horizonsend.ion.common.utils.text.GUI_MARGIN
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class NavigationInfoGui(
    val player: Player,
    private val name: String,
    private val icon: GuiItem,
    private val backButtonHandler: () -> Unit
) {

    private var currentWindow: Window? = null

    private fun createGui(): Gui {
        val gui = Gui.normal()

        gui.setStructure(
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            "x v . . o . . . ."
        )

        gui.addIngredient('v', GuiItems.CustomControlItem("Return To Galactic Menu", GuiItem.DOWN) {
            _: ClickType, _: Player, _: InventoryClickEvent -> backButtonHandler.invoke()
        })
            .addIngredient('x', GuiItems.closeMenuItem(player))
            .addIngredient('o', GuiItems.CustomControlItem(name, icon))

        return gui.build()
    }

    private fun createText(): Component {
        val header = "Information: $name"
        val guiText = GuiText(header)

        guiText.addBackground()
        val componentList = Component.text("random text that i wrote to try and fill this wrap function out; i don't know what to put here. skibidi edge rizz").wrap(DEFAULT_GUI_WIDTH - GUI_MARGIN)

        for ((index, component) in componentList.withIndex()) {
            guiText.add(component, line = index, verticalShift = GUI_HEADER_MARGIN)
        }

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