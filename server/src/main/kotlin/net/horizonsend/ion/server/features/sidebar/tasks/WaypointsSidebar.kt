package net.horizonsend.ion.server.features.sidebar.tasks

import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import org.bukkit.entity.Player

object WaypointsSidebar {
    fun splitRouteString(player: Player): List<String> {
        val string = WaypointManager.getRouteString(player)
        return string.chunked(MainSidebar.WAYPOINT_MAX_LENGTH)
    }
}