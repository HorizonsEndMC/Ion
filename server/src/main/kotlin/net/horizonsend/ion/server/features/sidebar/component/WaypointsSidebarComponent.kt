package net.horizonsend.ion.server.features.sidebar.component

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import java.util.function.Supplier

class WaypointsSidebarComponent(supplier: Supplier<String>) : SidebarComponent{
    val string = supplier.get()

    private fun getRouteComponent(): TextComponent {
        val component = text()
        for (char in string) {
            if (char == '\uE036') {
                component.append(text(char).font(key("horizonsend:sidebar")).color(GRAY))
            } else {
                component.append(text(char).font(key("horizonsend:sidebar")).color(AQUA))
            }
        }
        return component.build()
    }

    override fun draw(drawable: LineDrawable) {
        val line = text()
        line.append(text(">  ").color(DARK_GRAY))
        line.append(getRouteComponent())

        drawable.drawLine(line.build())
    }
}