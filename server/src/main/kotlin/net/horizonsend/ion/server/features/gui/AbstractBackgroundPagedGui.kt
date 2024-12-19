package net.horizonsend.ion.server.features.gui

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

interface AbstractBackgroundPagedGui {

    var currentWindow: Window?

    fun createGui(): PagedGui<Item> = PagedGui.items().build()

    fun createText(player: Player, currentPage: Int): Component = Component.empty()

    fun buildWindow(player: Player): Window {
        val gui = createGui()

        val window = Window.single()
            .setViewer(player)
            .setTitle(AdventureComponentWrapper(createText(player, 0)))
            .setGui(gui)
            .build()

        fun updateTitle(): (Int, Int) -> Unit {
            return { _, currentPage ->
                window.changeTitle(AdventureComponentWrapper(createText(player, currentPage)))
            }
        }

        gui.addPageChangeHandler(updateTitle())

        return window
    }

	fun refreshPageText(player: Player, gui: PagedGui<*>) {
		currentWindow?.changeTitle(AdventureComponentWrapper(createText(player, gui.currentPage)))
	}
}
