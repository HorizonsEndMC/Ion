package net.horizonsend.ion.server.features.gui

import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

abstract class AbstractBackgroundPagedGui(viewer: Player) : InvUIWindowWrapper(viewer) {
    open fun createGui(): PagedGui<Item> = PagedGui.items().build()

	override fun buildWindow(): Window {
        val gui = createGui()

        val window = Window.single()
            .setViewer(viewer)
            .setTitle(buildTitle())
            .setGui(gui)
            .build()

        return window
    }
}
