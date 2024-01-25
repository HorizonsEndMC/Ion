package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.X_CROSS_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.ROUTE_SEGMENT_ICON
import net.horizonsend.ion.server.features.waypoint.WaypointManager
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
        text(X_CROSS_ICON.text, RED).font(Sidebar.fontKey)
    } else {
        text(numJumps.toString(), AQUA)
    }

    private fun getColor(enabled: Boolean): NamedTextColor {
        return if (enabled) AQUA else GRAY
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("Route").style(style(BOLD).color(YELLOW)),
            text(" | ", DARK_GRAY),
            text(ROUTE_SEGMENT_ICON.text, getColor(compactWaypoints)).font(Sidebar.fontKey),
            text(" | ", DARK_GRAY),
            text("Jumps: ", GRAY),
            numJumpsComponent
        )
        drawable.drawLine(line)
    }
}