package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class ObjectiveHeaderSidebarComponent(player: Player) : SidebarComponent {
    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("Objective").style(style(BOLD).color(YELLOW)),
        )
        drawable.drawLine(line)
    }
}