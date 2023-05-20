package net.horizonsend.ion.server.features.sidebar

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import java.util.function.Supplier

class ContactsSidebarComponent(private val valueSupplier: Supplier<ContactsData>) : SidebarComponent {

    override fun draw(drawable: LineDrawable) {
        val value = valueSupplier.get()

        val line = Component.text()
        line.append(value.prefix)
        line.append(Component.text(" | ").color(NamedTextColor.DARK_GRAY))
        line.append(value.name)
        if (value.suffix != Component.empty()) {
            line.appendSpace()
            line.append(value.suffix)
        }
        line.append(value.heading)
        line.appendSpace()
        line.append(value.height)
        line.appendSpace()
        line.append(value.distance)

        drawable.drawLine(line.build())
    }
}
