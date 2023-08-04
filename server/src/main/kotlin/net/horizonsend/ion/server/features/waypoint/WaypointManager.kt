package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import org.bukkit.Location
import org.bukkit.entity.Player
import org.jgrapht.GraphTests
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
				edge.hyperspaceEdge = true
				graph.setEdgeWeight(edge, Hyperspace.INTER_SYSTEM_DISTANCE.toDouble())
			}
		}
	}

	fun reloadMainMap() {
		if (!GraphTests.isEmpty(mainMap)) {
			mainMap.removeAllEdges(mainMap.edgeSet())
			mainMap.removeAllVertices(mainMap.vertexSet())
			populateMapVertices(mainMap)
			populateMapEdges(mainMap)
		}
	}

	fun printMainMapVertices(player: Player) {
		for (vertex in mainMap.vertexSet()) {
			player.information(
				StringBuilder(vertex.name)
					.append(" at ${vertex.loc}")
					.append(" with companion vertex ${vertex.linkedWaypoint?.name}")
					.toString()
			)
		}
	}

	fun printMainMapEdges(player: Player) {
		for (edge in mainMap.edgeSet()) {
			player.information(
				StringBuilder("Edge from ")
					.append(mainMap.getEdgeSource(edge).name)
					.append(" -> ")
					.append(mainMap.getEdgeTarget(edge).name)
					.append(
						when (edge.hyperspaceEdge) {
							true -> " and is inter-system"
							else -> " is not inter-system"
						}
					)
					.toString()
			)
		}
	}

	fun getVertex(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>, name: String): WaypointVertex? {
		return graph.vertexSet().find { it.name == name }
	}
}

data class WaypointVertex(val name: String, val loc: Location, var linkedWaypoint: WaypointVertex? = null)

data class WaypointEdge(var hyperspaceEdge: Boolean = false) : DefaultWeightedEdge()
