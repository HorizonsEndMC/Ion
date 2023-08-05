package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jgrapht.GraphTests
import org.jgrapht.Graphs
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.util.*

object WaypointManager : IonServerComponent() {
    // mainGraph holds the server graph
    val mainGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)

    // playerGraphs hold copies of mainGraph, with add'l vertices per player (for shortest path calculation)
    val playerGraphs: MutableMap<UUID, SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>> = mutableMapOf()

    /**
     * server component handlers
     */
    override fun onEnable() {
        //reloadMainGraph()

        // update all graphs every five seconds
        /*
        Tasks.syncRepeat(0L, 100L) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (playerGraphs[player.uniqueId] != null) {
                    playerGraphs[player.uniqueId]?.let { playerGraph ->
                        updatePlayerPositionVertex(playerGraph, player)
                    }
                } else {
                    // add player's graph to the map
                    val playerGraph =
                        SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
                    clonePlayerGraphFromMain(playerGraph)
                    updatePlayerPositionVertex(playerGraph, player)
                    playerGraphs[player.uniqueId] = playerGraph
                }
            }
        }
         */
    }

    override fun onDisable() {
        for (player in playerGraphs.keys) {
            playerGraphs.remove(player)
        }
    }

    /**
     * helper functions
     */
    private fun getVertex(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        name: String
    ): WaypointVertex? {
        return graph.vertexSet().find { it.name == name }
    }

    private fun updateEdgeWeights(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        vertex: WaypointVertex
    ) {
        graph.removeAllEdges(graph.edgesOf(vertex))
        val verticesSameWorld = graph.vertexSet()
            .filter { otherVertex -> otherVertex.loc.world == vertex.loc.world && vertex != otherVertex }
        for (otherVertex in verticesSameWorld) {
            val edge = graph.addEdge(vertex, otherVertex)
            graph.setEdgeWeight(edge, vertex.loc.distance(otherVertex.loc))
        }
    }

    fun printGraphVertices(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>?, player: Player) {
        if (graph == null) {
            player.serverError("Graph does not exist")
            return
        }
        for (vertex in graph.vertexSet()) {
            player.information(
                StringBuilder(vertex.name)
                    .append(" at ${vertex.loc}")
                    .append(" with companion vertex ${vertex.linkedWaypoint}")
                    .toString()
            )
        }
    }

    fun printGraphEdges(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>?, player: Player) {
        if (graph == null) {
            player.serverError("Graph does not exist")
            return
        }
        for (edge in graph.edgeSet()) {
            val sourceVertex = graph.getEdgeSource(edge)
            val targetVertex = graph.getEdgeTarget(edge)
            val weight = graph.getEdgeWeight(edge)
            player.information(
                StringBuilder("Edge from ")
                    .append(sourceVertex.name)
                    .append(" -> ")
                    .append(targetVertex.name)
                    .append(
                        when (edge.hyperspaceEdge) {
                            true -> " and is inter-system"
                            else -> " is not inter-system"
                        }
                    )
                    .append(" with weight $weight")
                    .toString()
            )
        }
    }

    /**
     * mainGraph-specific functions
     */
    private fun populateMainGraphVertices() {
        // add all planets as vertices to mainGraph
        for (planet in Space.getPlanets()) {
            val vertex = WaypointVertex(
                name = planet.name,
                loc = planet.location.toLocation(planet.spaceWorld)
            )
            mainGraph.addVertex(vertex)
        }

        // add all beacons as vertices to mainGraph
        for (beacon in IonServer.configuration.beacons) {
            // 2 vertices for each beacon's entry and exit point
            val vertexEntry = WaypointVertex(
                name = beacon.name,
                loc = beacon.spaceLocation.toLocation()
            )
            val vertexExit = WaypointVertex(
                name = StringBuilder(beacon.name).append(" Exit").toString(),
                loc = beacon.destination.toLocation()
            )
            // link edge vertex with exit vertex (for edge connections later)
            vertexEntry.linkedWaypoint = vertexExit.name
            mainGraph.addVertex(vertexEntry)
            mainGraph.addVertex(vertexExit)
        }
    }

    private fun populateMainGraphEdges() {
        // add edges for each vertex
        for (vertex in mainGraph.vertexSet()) {
            // connect vertices that are in the same space world (and not itself) (celestials in the same world)
            val verticesSameWorld = mainGraph.vertexSet()
                .filter { otherVertex -> otherVertex.loc.world == vertex.loc.world && otherVertex != vertex }
            for (otherVertex in verticesSameWorld) {
                val edge = WaypointEdge(
                    source = vertex,
                    target = otherVertex,
                    hyperspaceEdge = false
                )
                if (!mainGraph.addEdge(vertex, otherVertex, edge)) {
                    println("EDGE BETWEEN $vertex AND $otherVertex FAILED TO GENERATE")
                }
                mainGraph.setEdgeWeight(edge, vertex.loc.distance(otherVertex.loc))
            }

            // add edges between vertices linked to another (i.e. beacons)
            if (vertex.linkedWaypoint != null) {
                val otherVertex = getVertex(mainGraph, vertex.linkedWaypoint!!)!!
                val edge = WaypointEdge(
                    source = vertex,
                    target = otherVertex,
                    hyperspaceEdge = true
                )
                if (!mainGraph.addEdge(vertex, otherVertex, edge)) {
                    println("EDGE BETWEEN $vertex AND ${vertex.linkedWaypoint} FAILED TO GENERATE")
                }
                edge.hyperspaceEdge = true
                mainGraph.setEdgeWeight(edge, Hyperspace.INTER_SYSTEM_DISTANCE.toDouble())
            }
        }
    }

    fun reloadMainGraph() {
        populateMainGraphVertices()
        populateMainGraphEdges()
    }

    /**
     * playerGraph-specific functions
     */
    private fun clonePlayerGraphFromMain(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
        Graphs.addGraph(graph, mainGraph)
    }

    private fun updatePlayerPositionVertex(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        player: Player
    ) {
        // get the vertex representing the player's position, or create one
        val locVertex = getVertex(graph, "Current Location")
        if (locVertex == null) {
            val newVertex = WaypointVertex(
                name = "Current Location",
                loc = player.location,
                linkedWaypoint = null
            )
            graph.addVertex(newVertex)
            updateEdgeWeights(graph, newVertex)
        } else {
            locVertex.loc = player.location
            updateEdgeWeights(graph, locVertex)
        }
    }

    /**
     * listeners
     */
    @Suppress("unused")
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // add player's graph to the map
        val playerGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
        clonePlayerGraphFromMain(playerGraph)
        playerGraphs[event.player.uniqueId] = playerGraph
    }

    @Suppress("unused")
    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        // remove player's graph from the map (maybe keep it)
        val playerGraph = playerGraphs[event.player.uniqueId] ?: return
        playerGraphs.remove(event.player.uniqueId)
    }

    @Suppress("unused")
    @EventHandler
    fun onPlayerTeleport(event: PlayerChangedWorldEvent) {
        // update the player's map upon a world change
        playerGraphs[event.player.uniqueId]?.let { playerGraph ->
            updatePlayerPositionVertex(playerGraph, event.player)
        }
    }
}

/**
 * data classes
 */
data class WaypointVertex(
    val name: String,
    var loc: Location,
    var linkedWaypoint: String? = null
)

data class WaypointEdge(
    val source: WaypointVertex,
    val target: WaypointVertex,
    var hyperspaceEdge: Boolean = false
) : DefaultWeightedEdge()
