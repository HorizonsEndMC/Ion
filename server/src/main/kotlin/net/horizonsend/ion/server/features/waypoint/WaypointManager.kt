package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jgrapht.GraphPath
import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.util.*

object WaypointManager : IonServerComponent() {
    // mainGraph holds the server graph
    val mainGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)

    // playerGraphs hold copies of mainGraph, with add'l vertices per player (for shortest path calculation)
    val playerGraphs: MutableMap<UUID, SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>> = mutableMapOf()
    val playerDestinations: MutableMap<UUID, MutableList<WaypointVertex>> = mutableMapOf()

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
    fun getVertex(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        name: String
    ): WaypointVertex? {
        return graph.vertexSet().find { it.name == name }
    }

    private fun connectVerticesInSameWorld(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        vertex: WaypointVertex
    ) {
        // find vertices that are in the same world as the current vertex; create them
        val verticesSameWorld = graph.vertexSet()
            .filter { otherVertex -> otherVertex.loc.world == vertex.loc.world && vertex != otherVertex }
        for (otherVertex in verticesSameWorld) {
            val outEdge = WaypointEdge(
                source = vertex,
                target = otherVertex,
                hyperspaceEdge = false
            )
            val inEdge = WaypointEdge(
                source = otherVertex,
                target = vertex,
                hyperspaceEdge = false
            )
            graph.addEdge(vertex, otherVertex, outEdge)
            graph.setEdgeWeight(outEdge, vertex.loc.distance(otherVertex.loc))
            graph.addEdge(otherVertex, vertex, inEdge)
            graph.setEdgeWeight(inEdge, otherVertex.loc.distance(vertex.loc))
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
                name = beacon.name.replace(" ", "_"),
                loc = beacon.spaceLocation.toLocation()
            )
            val vertexExit = WaypointVertex(
                name = StringBuilder(beacon.name.replace(" ", "_")).append("_Exit").toString(),
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
            connectVerticesInSameWorld(mainGraph, vertex)

            // add edges between vertices linked to another (i.e. beacons)
            if (vertex.linkedWaypoint != null) {
                val otherVertex = getVertex(mainGraph, vertex.linkedWaypoint!!)!!
                val edge = WaypointEdge(
                    source = vertex,
                    target = otherVertex,
                    hyperspaceEdge = true
                )
                mainGraph.addEdge(vertex, otherVertex, edge)
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
    fun updatePlayerGraph(player: Player) {
        if (playerGraphs[player.uniqueId] != null) {
            playerGraphs[player.uniqueId]?.let { playerGraph ->
                clonePlayerGraphFromMain(playerGraph)
                updatePlayerPositionVertex(playerGraph, player)
            }
        } else {
            // add player's graph to the map
            val playerGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
            clonePlayerGraphFromMain(playerGraph)
            updatePlayerPositionVertex(playerGraph, player)
            playerGraphs[player.uniqueId] = playerGraph
        }
    }

    fun clonePlayerGraphFromMain(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
        Graphs.addGraph(graph, mainGraph)
    }

    fun updatePlayerPositionVertex(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        player: Player
    ) {
        // get the vertex representing the player's position, or create one
        val locVertex = getVertex(graph, "Current Location")
        graph.removeVertex(locVertex)
        val newVertex = WaypointVertex(
            name = "Current Location",
            loc = player.location,
            linkedWaypoint = null
        )
        graph.addVertex(newVertex)
        connectVerticesInSameWorld(graph, newVertex)
    }

    fun findShortestPath(player: Player): List<GraphPath<WaypointVertex, WaypointEdge>> {
        if (playerDestinations[player.uniqueId].isNullOrEmpty()) {
            player.userError("No waypoints set")
            return listOf()
        } else {
            val shortestPaths: MutableList<GraphPath<WaypointVertex, WaypointEdge>> = mutableListOf()
            var currentVertex = playerGraphs[player.uniqueId]?.let { getVertex(it, "Current Location") }
            if (currentVertex == null) {
                player.serverError("Player graph not generated")
                return listOf()
            }

            for (destinationVertex in playerDestinations[player.uniqueId]!!) {
                shortestPaths.add(
                    DijkstraShortestPath.findPathBetween(
                        playerGraphs[player.uniqueId],
                        currentVertex,
                        destinationVertex
                    )
                )
                currentVertex = destinationVertex
            }

            return shortestPaths
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
