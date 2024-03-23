package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.BOOKMARK_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.GENERIC_STARSHIP_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.GUNSHIP_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.HYPERSPACE_BEACON_ENTER_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.PLANET_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STAR_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STATION_ICON
import net.kyori.adventure.text.Component.space
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
    private val contactsDistance = PlayerCache[player].contactsDistance
    private val starshipsEnabled = PlayerCache[player].contactsStarships
    private val lastStarshipEnabled = PlayerCache[player].lastStarshipEnabled
	private val planetsEnabled = PlayerCache[player].planetsEnabled
	private val starsEnabled = PlayerCache[player].starsEnabled
	private val beaconsEnabled = PlayerCache[player].beaconsEnabled
    private val stationsEnabled = PlayerCache[player].stationsEnabled
    private val bookmarksEnabled = PlayerCache[player].bookmarksEnabled

    private fun getColor(enabled: Boolean) : NamedTextColor {
        return if (enabled) AQUA else GRAY
    }

    override fun draw(drawable: LineDrawable) {
        val line = ofChildren(
            text("Contacts").style(style(BOLD).color(YELLOW)),
            text(" | ", DARK_GRAY),
            text(contactsDistance, AQUA),
            space(),
            text(GUNSHIP_ICON.text, getColor(starshipsEnabled)).font(Sidebar.fontKey),
            space(),
            text(GENERIC_STARSHIP_ICON.text, getColor(lastStarshipEnabled)).font(Sidebar.fontKey),
            space(),
            text(PLANET_ICON.text, getColor(planetsEnabled)).font(Sidebar.fontKey),
            space(),
            text(STAR_ICON.text, getColor(starsEnabled)).font(Sidebar.fontKey),
            space(),
            text(HYPERSPACE_BEACON_ENTER_ICON.text, getColor(beaconsEnabled)).font(Sidebar.fontKey),
            space(),
            text(STATION_ICON.text, getColor(stationsEnabled)).font(Sidebar.fontKey),
            space(),
            text(BOOKMARK_ICON.text, getColor(bookmarksEnabled)).font(Sidebar.fontKey)
        )
        drawable.drawLine(line)
    }
}
