package net.horizonsend.ion.server.features.sidebar

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class ContactsHeaderSidebarComponent(private val player: Player) : SidebarComponent {
    override fun draw(drawable: LineDrawable) {
        val line = text()
        line.append(text("Contacts").style(style(BOLD).color(YELLOW)))
        line.append(text(" | ").color(DARK_GRAY))
        line.append(text("\uE001").font(key("horizonsend:sidebar")).color(AQUA))
        line.appendSpace()
        line.append(text("\uE020").font(key("horizonsend:sidebar")).color(AQUA))
        line.appendSpace()
        line.append(text("\uE021").font(key("horizonsend:sidebar")).color(AQUA))
        line.appendSpace()
        line.append(text("\uE022").font(key("horizonsend:sidebar")).color(AQUA))

        drawable.drawLine(line.build())
    }
}