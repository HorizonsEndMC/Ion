package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.Sidebar
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.BOOKMARK_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.COMPASS_NEEDLE_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.GENERIC_STARSHIP_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.GUNSHIP_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.HYPERSPACE_BEACON_ENTER_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.LIST_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.PLANET_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.ROUTE_SEGMENT_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STAR_ICON
import net.horizonsend.ion.server.features.sidebar.SidebarIcon.STATION_ICON
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.entity.Player

@CommandAlias("sidebar")
object SidebarCommand : SLCommand() {
    @Default
    @Suppress("unused")
    fun defaultCase(sender: Player) {
        val starshipsComponentsEnabled = PlayerCache[sender.uniqueId].starshipsEnabled
        val advancedStarshipInfo = PlayerCache[sender.uniqueId].advancedStarshipInfo
        val rotateCompass = PlayerCache[sender.uniqueId].rotateCompass

        val contactsComponentsEnabled = PlayerCache[sender.uniqueId].contactsEnabled
        val starshipsEnabled = PlayerCache[sender.uniqueId].contactsStarships
        val lastStarshipEnabled = PlayerCache[sender.uniqueId].lastStarshipEnabled
        val planetsEnabled = PlayerCache[sender.uniqueId].planetsEnabled
        val starsEnabled = PlayerCache[sender.uniqueId].starsEnabled
        val beaconsEnabled = PlayerCache[sender.uniqueId].beaconsEnabled
        val stationsEnabled = PlayerCache[sender.uniqueId].stationsEnabled
        val bookmarksEnabled = PlayerCache[sender.uniqueId].bookmarksEnabled

        val waypointsComponentsEnabled = PlayerCache[sender.uniqueId].waypointsEnabled
        val compactWaypoints = PlayerCache[sender.uniqueId].compactWaypoints

        sender.sendMessage(ofChildren(
            lineBreakWithCenterText(text("Starship - Information about your current starship", HE_LIGHT_ORANGE), width=1), newline(),
            text("Starship information visible: $starshipsComponentsEnabled", WHITE), newline(),
            text("\\ N /", GRAY).append(text(" Cruise Compass", WHITE)), newline(),
            text("W ^ E", GRAY).append(text(" Indicates current and desired cruise direction", WHITE)), newline(),
            text("/ S \\", GRAY).append(text(" GOLD", GOLD)).append(text(" - Current cruise direction vector, ", WHITE))
                .append(text("AQUA", AQUA)).append(text(" - Desired cruise direction vector, ", WHITE))
                .append(text("GREEN", GREEN)).append(text(" - Current and desired cruise direction vectors matching", WHITE)), newline(), newline(),
            text("HULL: ", GRAY).append(text("Current hull percentage and block count", WHITE)), newline(),
            text("SPD: ", GRAY).append(text("Current speed, maximum speed, acceleration, and cruise/DC status", WHITE)), newline(),
            text("PM: ", GRAY).append(text("Current power mode for shields, weapons, and thrusters", WHITE)), newline(),
            text("CAP: ", GRAY).append(text("Current light weapons energy within ship capacitor", WHITE)), newline(),
            text("HVY: ", GRAY).append(text("Cooldown for heavy weapons", WHITE)), newline(),
            text("ACTIVE: ", GRAY).append(text("Active weapon sets and ship modules", WHITE)), newline(),
            newline(),
            text("Settings: "), newline(),
            text(LIST_ICON.text, getColor(advancedStarshipInfo)).font(Sidebar.fontKey), text(" advanced - Displays advanced information: $advancedStarshipInfo", WHITE), newline(),
            text(COMPASS_NEEDLE_ICON.text, getColor(rotateCompass)).font(Sidebar.fontKey), text(" rotatecompass - Top of compass faces direction that ship is facing: $rotateCompass", WHITE), newline(),
            lineBreak(47), newline(),
            newline(),
            lineBreakWithCenterText(text("Contacts - View nearby objects and their status", HE_LIGHT_ORANGE), width=3), newline(),
            text("Contacts information visible: $contactsComponentsEnabled", WHITE), newline(),
            text(GUNSHIP_ICON.text, getColor(starshipsEnabled)).font(Sidebar.fontKey), text(" starship - Other starships visible: $starshipsEnabled", WHITE), newline(),
            text(GENERIC_STARSHIP_ICON.text, getColor(lastStarshipEnabled)).font(Sidebar.fontKey), text(" laststarship - Last piloted starship visible: $lastStarshipEnabled", WHITE), newline(),
            text(PLANET_ICON.text, getColor(planetsEnabled)).font(Sidebar.fontKey), text(" planet - Planets visible: $planetsEnabled", WHITE), newline(),
            text(STAR_ICON.text, getColor(starsEnabled)).font(Sidebar.fontKey), text(" stars - Stars visible: $starsEnabled", WHITE), newline(),
            text(HYPERSPACE_BEACON_ENTER_ICON.text, getColor(beaconsEnabled)).font(Sidebar.fontKey), text(" beacons - Hyperspace beacons visible: $beaconsEnabled", WHITE), newline(),
            text(STATION_ICON.text, getColor(stationsEnabled)).font(Sidebar.fontKey), text(" stations - Stations and siege stations visible: $stationsEnabled", WHITE), newline(),
            text(BOOKMARK_ICON.text, getColor(bookmarksEnabled)).font(Sidebar.fontKey), text(" bookmarks - Bookmarks visible: $bookmarksEnabled", WHITE), newline(),
            lineBreak(47), newline(),
            newline(),
            lineBreakWithCenterText(text("Route - Current plotted route", HE_LIGHT_ORANGE), width=9), newline(),
            text("Route information visible: $waypointsComponentsEnabled", WHITE), newline(),
            text(ROUTE_SEGMENT_ICON.text, getColor(compactWaypoints)).font(Sidebar.fontKey), text(" compactwaypoints - Display route segments between destinations: $compactWaypoints"), newline(),
        ))
    }

    private fun getColor(enabled: Boolean) : NamedTextColor {
        return if (enabled) AQUA else GRAY
    }
}