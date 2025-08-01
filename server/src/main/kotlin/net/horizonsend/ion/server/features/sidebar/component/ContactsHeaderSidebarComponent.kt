package net.horizonsend.ion.server.features.sidebar.component

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
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
    private val contactsDistance = player.getSetting(PlayerSettings::contactsDistance)
    private val starshipsEnabled = player.getSetting(PlayerSettings::contactsStarships)
    private val lastStarshipEnabled = player.getSetting(PlayerSettings::lastStarshipEnabled)
	private val planetsEnabled = player.getSetting(PlayerSettings::planetsEnabled)
	private val starsEnabled = player.getSetting(PlayerSettings::starsEnabled)
	private val beaconsEnabled = player.getSetting(PlayerSettings::beaconsEnabled)
    private val stationsEnabled = player.getSetting(PlayerSettings::stationsEnabled)
    private val bookmarksEnabled = player.getSetting(PlayerSettings::bookmarksEnabled)

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
