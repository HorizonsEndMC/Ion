package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.Style.style
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.entity.Player

class WaypointsHeaderSidebarComponent(player: Player) : SidebarComponent {
    private val compactWaypoints = PlayerCache[player].compactWaypoints
    private val numJumps = WaypointManager.playerNumJumps[player.uniqueId] ?: -1
    private val numJumpsComponent = if (numJumps == -1) {
        text("\uE031").font(key("horizonsend:sidebar")).color(RED)
    } else {
        text(numJumps.toString()).style(style().color(AQUA))
    }

    private fun getColor(enabled: Boolean): NamedTextColor {
        return if (enabled) AQUA else GRAY
    }

    override fun draw(drawable: LineDrawable) {
        val line = text()
        line.append(text("Route").style(style(BOLD).color(YELLOW)))
        line.append(text(" | ").color(DARK_GRAY))
        line.append(text("\uE036").font(key("horizonsend:sidebar")).color(getColor(compactWaypoints)))
        line.append(text(" | ").color(DARK_GRAY))
        line.append(text("Jumps: ").style(style().color(GRAY)))
        line.append(numJumpsComponent)

        drawable.drawLine(line.build())
    }
}