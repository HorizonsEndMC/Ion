package net.horizonsend.ion.server.features.sidebar.component

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import java.util.function.Supplier

class WaypointsNameSidebarComponent(supplier: Supplier<String>, val destination: Boolean) : SidebarComponent {
    val string = supplier.get()

    private fun getDestinationColor(destination: Boolean): NamedTextColor {
        return if (destination) GREEN else RED
    }

    override fun draw(drawable: LineDrawable) {
        val line = text()
        if (destination) {
            line.append(text("Final: ").color(GRAY))
        } else {
            line.append(text("Next: ").color(GRAY))
        }
        line.append(text(string).color(getDestinationColor(destination)))
        drawable.drawLine(line.build())
    }
}