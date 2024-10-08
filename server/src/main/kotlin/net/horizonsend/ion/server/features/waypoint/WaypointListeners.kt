package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class WaypointListeners : SLEventListener() {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // add player's graph to the map
        WaypointManager.updatePlayerGraph(event.player)
        WaypointManager.playerDestinations[event.player.uniqueId] = mutableListOf()
        WaypointManager.playerPaths[event.player.uniqueId] = mutableListOf()
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        // remove player's graph from the map (maybe keep it)
        WaypointManager.playerGraphs.remove(event.player.uniqueId)
        WaypointManager.playerDestinations.remove(event.player.uniqueId)
        WaypointManager.playerPaths.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerChangedWorldEvent) {
        // update the player's map upon a world change
        WaypointManager.updatePlayerGraph(event.player)
        WaypointManager.checkWaypointReached(event.player)
        WaypointManager.updatePlayerPaths(event.player)
        WaypointManager.updateNumJumps(event.player)
    }
}
