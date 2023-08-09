package net.horizonsend.ion.server.features.sidebar.bars

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class ContactsHeaderSidebarComponent(player: Player) : SidebarComponent {
    private val starshipsEnabled = PlayerCache[player].contactsStarships
    private val lastStarshipEnabled = PlayerCache[player].lastStarshipEnabled
	private val planetsEnabled = PlayerCache[player].planetsEnabled
	private val starsEnabled = PlayerCache[player].starsEnabled
	private val beaconsEnabled = PlayerCache[player].beaconsEnabled

    private fun getColor(enabled: Boolean) : NamedTextColor {
        return if (enabled) AQUA else GRAY
    }

    override fun draw(drawable: LineDrawable) {
        val line = text()
        line.append(text("Contacts").style(style(BOLD).color(YELLOW)))
        line.append(text(" | ").color(DARK_GRAY))
        line.append(text("\uE001").font(key("horizonsend:sidebar")).color(getColor(starshipsEnabled)))
        line.appendSpace()
        line.append(text("\uE032").font(key("horizonsend:sidebar")).color(getColor(lastStarshipEnabled)))
        line.appendSpace()
        line.append(text("\uE020").font(key("horizonsend:sidebar")).color(getColor(planetsEnabled)))
        line.appendSpace()
        line.append(text("\uE021").font(key("horizonsend:sidebar")).color(getColor(starsEnabled)))
        line.appendSpace()
        line.append(text("\uE022").font(key("horizonsend:sidebar")).color(getColor(beaconsEnabled)))

        drawable.drawLine(line.build())
    }
}
