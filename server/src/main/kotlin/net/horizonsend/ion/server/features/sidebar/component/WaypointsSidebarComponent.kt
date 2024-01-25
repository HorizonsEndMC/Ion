package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.ROUTE_SEGMENT_ICON
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import java.util.function.Supplier

class WaypointsSidebarComponent(supplier: Supplier<String>) : SidebarComponent {
    val string = supplier.get()

    private fun getRouteComponent(): TextComponent {
        val component = text()
        for (char in string) {
            if (char == ROUTE_SEGMENT_ICON.text.first()) {
                component.append(text(char, GRAY).font(Sidebar.fontKey))
            } else {
                component.append(text(char, AQUA).font(Sidebar.fontKey))
            }
        }
        return component.build()
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            space(),
            text("> ", DARK_GRAY),
            getRouteComponent()
        )
        drawable.drawLine(line)
    }
}