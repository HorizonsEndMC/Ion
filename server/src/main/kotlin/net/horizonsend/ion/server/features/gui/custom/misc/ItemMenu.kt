package net.horizonsend.ion.server.features.gui.custom.misc

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.min

class ItemMenu(
    val title: Component,
    val player: Player,
    private val guiItems: Collection<Item>,
    private val backButtonHandler: (Player) -> Unit
) {
    private var currentWindow: Window? = null
    private lateinit var gui: Gui

    private fun createGui(): Gui {
        val rows = ((guiItems.size + 8) / 9).coerceIn(1, 5) + 1
        // create a GUI that can store the number of elements in guiItems, plus an additional row for control items
        gui = Gui.empty(9, rows)

        // add all of guiItems or the first 45 items, whichever is smaller
        gui.addItems(*(guiItems.toList().subList(0, min(guiItems.size, 9 * 5))).toTypedArray())

        gui.setItem(0, rows - 1, backButton)

        return gui
    }

    private fun open(player: Player): Window {
        createGui()
        return Window.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle(AdventureComponentWrapper(title))
            .build()
    }

    fun openMainWindow() {
        currentWindow = open(player).apply { open() }
    }

    private val backButton = GuiItems.CustomControlItem(
        "Back", GuiItem.DOWN, callback = { _: ClickType, player: Player, _: InventoryClickEvent ->
            player.closeInventory()
            backButtonHandler.invoke(player)
        }
    )
}