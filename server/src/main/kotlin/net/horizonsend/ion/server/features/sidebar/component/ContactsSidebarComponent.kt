package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.sidebar.tasks.ContactsSidebar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import java.util.function.Supplier

class ContactsSidebarComponent(private val valueSupplier: Supplier<ContactsSidebar.ContactsData>) : SidebarComponent {

    override fun draw(drawable: LineDrawable) {
        val value = valueSupplier.get()

        val line = ofChildren(
            value.prefix,
            space(),
            value.heading,
            space(),
            value.height,
            space(),
            value.distance,
            value.padding,
            value.name,
            if (value.suffix != Component.empty()) {
                space().append(value.suffix)
            } else Component.empty()
        )

        drawable.drawLine(line)
    }
}
