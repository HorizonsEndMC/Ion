package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.listen
import net.horizonsend.ion.server.miscellaneous.utils.repeatString
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jgrapht.GraphPath
import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.util.*
import kotlin.math.ceil

object WaypointManager : IonServerComponent() {
    // mainGraph holds the server graph
    val mainGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)

    // playerGraphs hold copies of mainGraph, with additional vertices per player (for shortest path calculation)
    val playerGraphs: MutableMap<UUID, SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>> = mutableMapOf()
    val playerDestinations: MutableMap<UUID, MutableList<WaypointVertex>> = mutableMapOf()
    val playerPaths: MutableMap<UUID, List<GraphPath<WaypointVertex, WaypointEdge>>> = mutableMapOf()
    val playerTempWaypoints: MutableMap<UUID, MutableMap<WaypointVertex, Boolean>> = mutableMapOf()
    val playerNumJumps: MutableMap<UUID, Int> = mutableMapOf()

    const val MAX_DESTINATIONS = 5
    private const val WAYPOINT_REACHED_DISTANCE = 500

    /**
     * server component handlers
     */
    override fun onEnable() {
        // disabled for now as this crashes the server on startup
        Tasks.sync {
            reloadMainGraph()
        }

        // update all player graphs every five seconds if they have a destination saved
        // JGraphT is not thread safe; this cannot be async
        Tasks.syncRepeat(0L, 100L) {
            Bukkit.getOnlinePlayers().forEach { player ->
                if (playerDestinations.isNotEmpty()) {
                    updatePlayerGraph(player)
                    checkWaypointReached(player)
                    updatePlayerPaths(player)
                    updateNumJumps(player)
                }
            }
        }

        listen<StarshipPilotedEvent> { event ->
            updatePlayerGraph(event.player)
            updatePlayerPaths(event.player)
            updateNumJumps(event.player)
        }

        listen<StarshipUnpilotedEvent> { event ->
            updatePlayerGraph(event.player)
            updatePlayerPaths(event.player)
            updateNumJumps(event.player)
        }
    }

    override fun onDisable() {
        playerGraphs.clear()
        playerDestinations.clear()
    }

    /**
     * print/debug functions
     */
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
        // find vertices that are in the same world as the current vertex
        val verticesSameWorld = graph.vertexSet()
            .filter { otherVertex -> otherVertex.loc.world == vertex.loc.world && vertex != otherVertex }
        // create edges
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
            // add edges to graph and set edge weights (returns false if already exists)
            graph.addEdge(vertex, otherVertex, outEdge)
            graph.setEdgeWeight(outEdge, vertex.loc.distance(otherVertex.loc))
            graph.addEdge(otherVertex, vertex, inEdge)
            graph.setEdgeWeight(inEdge, otherVertex.loc.distance(vertex.loc))
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
                icon = '\uE020',
                loc = planet.location.toLocation(planet.spaceWorld)
            )
            mainGraph.addVertex(vertex)
        }

        // add all beacons as vertices to mainGraph
        for (beacon in IonServer.configuration.beacons) {
            // 2 vertices for each beacon's entry and exit point
            val vertexEntry = WaypointVertex(
                name = beacon.name.replace(" ", "_"),
                icon = '\uE022',
                loc = beacon.spaceLocation.toLocation()
            )
            val vertexExit = WaypointVertex(
                name = StringBuilder(beacon.name.replace(" ", "_")).append("_Exit").toString(),
                icon = '\uE034',
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
        // may break if celestial objects are removed after the graph is initially created
        populateMainGraphVertices()
        populateMainGraphEdges()
    }

    /**
     * playerGraph-specific functions
     */
    fun updatePlayerGraph(player: Player) {
        if (playerGraphs[player.uniqueId] != null) {
            // player already has a graph; update
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

    private fun clonePlayerGraphFromMain(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
        Graphs.addGraph(graph, mainGraph)
    }

    private fun updatePlayerPositionVertex(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        player: Player
    ) {
        // get the vertex representing the player's position, or create one
        val locVertex = getVertex(graph, "Current Location")
        // removeVertex also removes all edges touching the vertex
        graph.removeVertex(locVertex)
        val newVertex = WaypointVertex(
            name = "Current Location",
            icon = '\uE035',
            loc = player.location,
            linkedWaypoint = null
        )
        graph.addVertex(newVertex)
        connectVerticesInSameWorld(graph, newVertex)
    }

    fun addTempVertex(player: Player, loc: Location): WaypointVertex? {
        val graph = playerGraphs[player.uniqueId] ?: return null
        val newVertex = WaypointVertex(
            name = "Waypoint @ ${loc.world.name} (${loc.x.toInt()}, ${loc.z.toInt()})",
            icon = '\uE035',
            loc = loc,
            linkedWaypoint = null
        )
        if (playerTempWaypoints[player.uniqueId].isNullOrEmpty()) {
            playerTempWaypoints[player.uniqueId] = mutableMapOf()
            playerTempWaypoints[player.uniqueId]!![newVertex] = false
        } else {
            playerTempWaypoints[player.uniqueId]!![newVertex] = false
        }

        graph.addVertex(newVertex)
        connectVerticesInSameWorld(graph, newVertex)
        return newVertex
    }

    fun checkTempVertex(player: Player) {
        val tempWaypoints = playerTempWaypoints[player.uniqueId]
        if (tempWaypoints.isNullOrEmpty()) return

        for (waypoint in tempWaypoints) {
            if (waypoint.value) {
                // waypoint marked for deletion
                playerGraphs[player.uniqueId]!!.removeVertex(waypoint.key)
                playerTempWaypoints[player.uniqueId]!!.remove(waypoint.key)
            }
        }
    }

    /**
     * destination and path functions
     */
    fun addDestination(player: Player, vertex: WaypointVertex): Boolean {
        return if (playerDestinations[player.uniqueId].isNullOrEmpty()) {
            // list not created
            playerDestinations[player.uniqueId] = mutableListOf(vertex)
            true
        } else if (playerDestinations[player.uniqueId]!!.size >= MAX_DESTINATIONS) {
            // list is full
            false
        } else {
            // list exists
            playerDestinations[player.uniqueId]!!.add(vertex)
            true
        }
    }

    fun findShortestPath(player: Player): List<GraphPath<WaypointVertex, WaypointEdge>>? {
        // check if player has destination(s) set
        if (playerDestinations[player.uniqueId].isNullOrEmpty()) {
            return listOf()
        } else {
            // iterate through destinations and get the shortest path for each leg
            val shortestPaths: MutableList<GraphPath<WaypointVertex, WaypointEdge>> = mutableListOf()
            var currentVertex: WaypointVertex? =
                playerGraphs[player.uniqueId]?.let { getVertex(it, "Current Location") } ?: return listOf()

            for (destinationVertex in playerDestinations[player.uniqueId]!!) {
                val path = DijkstraShortestPath.findPathBetween(
                    playerGraphs[player.uniqueId],
                    currentVertex,
                    destinationVertex
                ) ?: return null
                shortestPaths.add(path)
                currentVertex = destinationVertex
            }

            // return path if it can be found
            // return listOf() if prerequisites cannot be met
            // return null if path cannot be found (do not overwrite
            return shortestPaths
        }
    }

    fun updatePlayerPaths(player: Player) {
        val pathList = findShortestPath(player)
        // null pathList implies destinations exist but route cannot be found; save the current path
        // e.g. if the player is on a planet or in hyperspace
        if (pathList != null) {
            playerPaths[player.uniqueId] = pathList
        }
    }

    fun checkWaypointReached(player: Player) {
        // check if player has destination(s) set
        if (playerDestinations[player.uniqueId].isNullOrEmpty()) {
            return
        } else {
            val vertex = playerDestinations[player.uniqueId]?.first() ?: return
            if (player.location.world == vertex.loc.world &&
                player.location.distance(vertex.loc) <= WAYPOINT_REACHED_DISTANCE) {
                playerDestinations[player.uniqueId]?.removeFirstOrNull()

                if (playerTempWaypoints[player.uniqueId]!!.containsKey(vertex)) {
                    playerTempWaypoints[player.uniqueId]!![vertex] = true
                }
            }
        }
    }

    private fun getNumJumps(player: Player, edge: WaypointEdge): Int {
        // if not piloting or no waypoints are set, no need to display
        val starship = PilotedStarships[player] ?: return -1

        // if navcomp or hyperdrive are not present, jumps are impossible
        val navComp = Hyperspace.findNavComp(starship) ?: return -1
        Hyperspace.findHyperdrive(starship) ?: return -1

        val maxRange = (navComp.multiblock.baseRange * starship.data.starshipType.actualType.hyperspaceRangeMultiplier)

        return if (edge.hyperspaceEdge) 1
        else ceil(playerGraphs[player.uniqueId]!!.getEdgeWeight(edge) / maxRange).toInt()
    }

    private fun getTotalNumJumps(player: Player): Int {
        val paths = playerPaths[player.uniqueId] ?: return -1
        if (paths.isEmpty()) return -1

        var numJumps = 0
        for (path in paths) {
            for (edge in path.edgeList) {
                val jumps = getNumJumps(player, edge)
                // no path set, or not piloting
                if (jumps == -1) return -1
                numJumps += jumps
            }
        }
        return numJumps
    }

    fun updateNumJumps(player: Player) {
        playerNumJumps[player.uniqueId] = getTotalNumJumps(player)
    }

    fun getRouteString(player: Player): String {
        val paths = playerPaths[player.uniqueId] ?: return ""
        val compactWaypoints = PlayerCache[player].compactWaypoints
        val str = StringBuilder()

        for (path in paths) {
            for (edge in path.edgeList) {
                val jumps = getNumJumps(player, edge)
                if (compactWaypoints && jumps != -1) {
                    str.append(repeatString("\uE036", jumps - 1))
                }
                str.append(edge.target.icon)
            }
        }
        return str.toString()
    }
}

/**
 * data classes
 */
data class WaypointVertex(
    val name: String,
    val icon: Char,
    var loc: Location,
    var linkedWaypoint: String? = null
)

data class WaypointEdge(
    val source: WaypointVertex,
    val target: WaypointVertex,
    var hyperspaceEdge: Boolean = false
) : DefaultWeightedEdge()
