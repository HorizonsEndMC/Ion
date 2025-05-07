package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import org.bukkit.entity.Player

@CommandAlias("sidebar")
object SidebarWaypointsCommand : SLCommand() {
    @Subcommand("route")
    fun defaultCase(
        sender: Player
    ) {
        sender.userError("Usage: /sidebar route <option> [toggle]")
    }

    @Subcommand("route enable")
    fun onEnableWaypoints(
        sender: Player
    ) {
		sender.setSetting(PlayerSettings::waypointsEnabled, true)
        sender.success("Enabled route on sidebar")
    }

    @Subcommand("route disable")
    fun onDisableWaypoints(
        sender: Player
    ) {
		sender.setSetting(PlayerSettings::waypointsEnabled, false)

		sender.success("Disabled route on sidebar")
    }

    @Subcommand("route compactWaypoints")
    @Description("Toggles compact waypoints; intermediate jumps are not displayed during navigation")
    fun onToggleCompactWaypoints(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val waypointsCompactWaypoints = toggle ?: !sender.getSetting(PlayerSettings::compactWaypoints)
		sender.setSetting(PlayerSettings::compactWaypoints, waypointsCompactWaypoints)

		sender.success("Changed compact waypoints visibility to $waypointsCompactWaypoints")
    }
}
