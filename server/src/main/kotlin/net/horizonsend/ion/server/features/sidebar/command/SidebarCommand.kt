package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.createTextDisplay
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player

@CommandAlias("sidebar")
object SidebarCommand : SLCommand() {
    @Default
    @Suppress("unused")
    fun defaultCase(sender: Player, @Optional page: Int?) {
        val body = formatPaginatedMenu(
            entries = listOf(starshipComponents(sender), contactsComponents(sender), routeComponents(sender)),
            command = "/sidebar",
            currentPage = page ?: 1,
            maxPerPage = 1,
        )

        sender.sendMessage(body)
    }

    @Subcommand("component")
    @Suppress("unused")
    fun onComponentTest(sender: Player) {
        val billboardText = createTextDisplay(sender)
        billboardText.text(text(COMPASS_NEEDLE_ICON.text).font(Sidebar.fontKey))
        billboardText.backgroundColor = Color.fromARGB(0x00000000)
        billboardText.billboard = Display.Billboard.CENTER

        sendEntityPacket(
            sender,
            billboardText.getNMSData(sender.eyeLocation.x, sender.eyeLocation.y, sender.eyeLocation.z),
            5 * 20L
        )
    }

    private fun starshipComponents(player: Player) : Component {
        val starshipsComponentsEnabled = PlayerCache[player.uniqueId].starshipsEnabled
        val advancedStarshipInfo = PlayerCache[player.uniqueId].advancedStarshipInfo
        val rotateCompass = PlayerCache[player.uniqueId].rotateCompass

        return ofChildren(
            lineBreakWithCenterText(
                text("Starship - Information about your current starship", HE_LIGHT_ORANGE),
                width = 1
            ),
            newline(),
            text("Starship information visible: $starshipsComponentsEnabled", WHITE),
            newline(),
            text("\\ N /", GRAY).append(text(" Cruise Compass", WHITE)),
            newline(),
            text("W ^ E", GRAY).append(text(" Indicates current and desired cruise direction", WHITE)),
            newline(),
            text("/ S \\", GRAY).append(text(" GOLD", GOLD)).append(text(" - Current cruise direction vector, ", WHITE))
                .append(text("AQUA", AQUA)).append(text(" - Desired cruise direction vector, ", WHITE))
                .append(text("GREEN", GREEN))
                .append(text(" - Current and desired cruise direction vectors matching", WHITE)),
            newline(),
            newline(),
            text("HULL: ", GRAY).append(text("Current hull percentage and block count", WHITE)),
            newline(),
            text("SPD: ", GRAY).append(text("Current speed, maximum speed, acceleration, and cruise/DC status", WHITE)),
            newline(),
            text("PM: ", GRAY).append(text("Current power mode for shields, weapons, and thrusters", WHITE)),
            newline(),
            text("CAP: ", GRAY).append(text("Current light weapons energy within ship capacitor", WHITE)),
            newline(),
            text("HVY: ", GRAY).append(text("Cooldown for heavy weapons", WHITE)),
            newline(),
            text("ACTIVE: ", GRAY).append(text("Active weapon sets and ship modules", WHITE)),
            newline(),
            newline(),
            text("Settings: "),
            newline(),
            text(LIST_ICON.text, getColor(advancedStarshipInfo)).font(Sidebar.fontKey),
            text(" advanced - Displays advanced information: $advancedStarshipInfo", WHITE),
            newline(),
            text(COMPASS_NEEDLE_ICON.text, getColor(rotateCompass)).font(Sidebar.fontKey),
            text(" rotatecompass - Top of compass faces direction that ship is facing: $rotateCompass", WHITE),
            newline(),
            lineBreak(47),
        )
    }

    private fun contactsComponents(player: Player) : Component {
        val contactsComponentsEnabled = PlayerCache[player.uniqueId].contactsEnabled
        val starshipsEnabled = PlayerCache[player.uniqueId].contactsStarships
        val lastStarshipEnabled = PlayerCache[player.uniqueId].lastStarshipEnabled
        val planetsEnabled = PlayerCache[player.uniqueId].planetsEnabled
        val starsEnabled = PlayerCache[player.uniqueId].starsEnabled
        val beaconsEnabled = PlayerCache[player.uniqueId].beaconsEnabled
        val stationsEnabled = PlayerCache[player.uniqueId].stationsEnabled
        val bookmarksEnabled = PlayerCache[player.uniqueId].bookmarksEnabled

        return ofChildren(
        lineBreakWithCenterText(
            text("Contacts - View nearby objects and their status", HE_LIGHT_ORANGE),
            width = 3
        ),
        newline(),
        text("Contacts information visible: $contactsComponentsEnabled", WHITE),
        newline(),
        text(GUNSHIP_ICON.text, getColor(starshipsEnabled)).font(Sidebar.fontKey),
        text(" starship - Other starships visible: $starshipsEnabled", WHITE),
        newline(),
        text(GENERIC_STARSHIP_ICON.text, getColor(lastStarshipEnabled)).font(Sidebar.fontKey),
        text(" laststarship - Last piloted starship visible: $lastStarshipEnabled", WHITE),
        newline(),
        text(PLANET_ICON.text, getColor(planetsEnabled)).font(Sidebar.fontKey),
        text(" planet - Planets visible: $planetsEnabled", WHITE),
        newline(),
        text(STAR_ICON.text, getColor(starsEnabled)).font(Sidebar.fontKey),
        text(" stars - Stars visible: $starsEnabled", WHITE),
        newline(),
        text(HYPERSPACE_BEACON_ENTER_ICON.text, getColor(beaconsEnabled)).font(Sidebar.fontKey),
        text(" beacons - Hyperspace beacons visible: $beaconsEnabled", WHITE),
        newline(),
        text(STATION_ICON.text, getColor(stationsEnabled)).font(Sidebar.fontKey),
        text(" stations - Stations and siege stations visible: $stationsEnabled", WHITE),
        newline(),
        text(BOOKMARK_ICON.text, getColor(bookmarksEnabled)).font(Sidebar.fontKey),
        text(" bookmarks - Bookmarks visible: $bookmarksEnabled", WHITE),
        newline(),
        lineBreak(47),
        )
    }

    private fun routeComponents(player: Player) : Component {
        val waypointsComponentsEnabled = PlayerCache[player.uniqueId].waypointsEnabled
        val compactWaypoints = PlayerCache[player.uniqueId].compactWaypoints

        return ofChildren(
            lineBreakWithCenterText(text("Route - Current plotted route", HE_LIGHT_ORANGE), width = 9),
            newline(),
            text("Route information visible: $waypointsComponentsEnabled", WHITE),
            newline(),
            text(ROUTE_SEGMENT_ICON.text, getColor(compactWaypoints)).font(Sidebar.fontKey),
            text(" compactwaypoints - Display route segments between destinations: $compactWaypoints"),
            newline(),
            lineBreak(47),
        )
    }

    private fun getColor(enabled: Boolean): NamedTextColor {
        return if (enabled) AQUA else GRAY
    }
}