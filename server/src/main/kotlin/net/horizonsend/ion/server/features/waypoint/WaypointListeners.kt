package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jgrapht.graph.SimpleDirectedWeightedGraph

class WaypointListeners : SLEventListener() {
    @Suppress("unused")
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // add player's graph to the map
        val playerGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
        WaypointManager.clonePlayerGraphFromMain(playerGraph)
        WaypointManager.playerGraphs[event.player.uniqueId] = playerGraph
        WaypointManager.playerDestinations[event.player.uniqueId] = mutableListOf()
    }

    @Suppress("unused")
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        // remove player's graph from the map (maybe keep it)
        WaypointManager.playerGraphs.remove(event.player.uniqueId)
        WaypointManager.playerDestinations.remove(event.player.uniqueId)
    }

    @Suppress("unused")
    @EventHandler
    fun onPlayerTeleport(event: PlayerChangedWorldEvent) {
        // update the player's map upon a world change
        WaypointManager.playerGraphs[event.player.uniqueId]?.let { playerGraph ->
            WaypointManager.updatePlayerPositionVertex(playerGraph, event.player)
        }
    }
}