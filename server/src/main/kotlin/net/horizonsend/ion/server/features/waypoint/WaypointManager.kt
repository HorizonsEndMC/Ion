package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.Space
import org.bukkit.Location
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

object WaypointManager : IonComponent() {
    val mainMap = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)

    override fun onEnable() {
        populateMapVertices(mainMap)
        populateMapEdges(mainMap)
    }

    private fun populateMapVertices(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
        // add all planets as vertices to graph
        for (planet in Space.getPlanets()) {
            val wp = WaypointVertex(
                name = planet.planetWorldName,
                loc = planet.location.toLocation(planet.spaceWorld)
            )
            graph.addVertex(wp)
        }

        // add all beacons as vertices to graph
        for (beacon in IonServer.configuration.beacons) {
            // 2 vertices for each beacon's entry and exit point
            val wpEntry = WaypointVertex(
                name = StringBuilder(beacon.name).append(" Entry").toString(),
                loc = beacon.spaceLocation.toLocation()
            )
            val wpExit = WaypointVertex(
                name = StringBuilder(beacon.name).append(" Exit").toString(),
                loc = beacon.destination.toLocation()
            )
            // link vertices with each other (for edge connections later)
            wpEntry.linkedWaypoint = wpExit
            wpExit.linkedWaypoint = wpEntry
            mainMap.addVertex(wpEntry)
            mainMap.addVertex(wpExit)
        }
    }

    private fun populateMapEdges(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
        // add edges for each vertex
        for (vertex in graph.vertexSet()) {
            // connect vertices that are in the same space world (and not itself)
            for (otherVertex in graph.vertexSet()) {
                if (vertex == otherVertex) continue
                if (vertex.loc.world == otherVertex.loc.world) {
                    val edge = graph.addEdge(vertex, otherVertex)
                    graph.setEdgeWeight(edge, vertex.loc.distance(otherVertex.loc))
                }
            }

            // add edges between vertices linked to another
            if (vertex.linkedWaypoint != null) {
                val edge = graph.addEdge(vertex, vertex.linkedWaypoint)
                graph.setEdgeWeight(edge, 60000.0)
            }
        }
    }
}

data class WaypointVertex(val name: String, val loc: Location) {
    var linkedWaypoint: WaypointVertex? = null
}

data class WaypointEdge(val source: WaypointVertex, val destination: WaypointVertex) : DefaultWeightedEdge() {
    var hyperspaceEdge = false

    init {
        if (source.loc.world != destination.loc.world) {
            hyperspaceEdge = true
        }
    }
}