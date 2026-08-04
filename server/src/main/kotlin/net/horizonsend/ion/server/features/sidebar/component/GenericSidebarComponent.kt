package net.horizonsend.ion.server.features.sidebar.component

import net.kyori.adventure.text.Component
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class GenericSidebarComponent(val component: Component) : SidebarComponent {
    override fun draw(drawable: LineDrawable) {
        drawable.drawLine(component)
    }
}