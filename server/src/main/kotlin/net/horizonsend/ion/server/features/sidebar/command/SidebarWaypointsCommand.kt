package net.horizonsend.ion.server.features.sidebar.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player
import org.litote.kmongo.set
import org.litote.kmongo.setTo

@CommandAlias("sidebar")
object SidebarWaypointsCommand : SLCommand() {
    @Default
    @Suppress("unused")
    @Subcommand("waypoints")
    fun defaultCase(
        sender: Player
    ) {
        sender.userError("Usage: /sidebar waypoints <option> [toggle]")
    }

    @Suppress("unused")
    @Subcommand("route enable")
    fun onEnableWaypoints(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::waypointsEnabled setTo true))
        sender.success("Enabled route on sidebar")
    }

    @Suppress("unused")
    @Subcommand("route disable")
    fun onDisableWaypoints(
        sender: Player
    ) {
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::waypointsEnabled setTo false))
        sender.success("Disabled route on sidebar")
    }

    @Suppress("unused")
    @Subcommand("route compactWaypoints")
    @Description("Toggles compact waypoints; intermediate jumps are not displayed during navigation")
    fun onToggleCompactWaypoints(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val waypointsCompactWaypoints = toggle ?: !PlayerCache[sender].compactWaypoints
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::compactWaypoints setTo waypointsCompactWaypoints))
        sender.success("Changed compact waypoints visibility to $waypointsCompactWaypoints")
    }
}