package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jgrapht.GraphTests
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.util.*

object WaypointManager : IonServerComponent() {
	val mainGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
	val playerGraphs: MutableMap<UUID, SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>> = mutableMapOf()

	override fun onEnable() {
		populateGraphVertices(mainGraph)
		populateGraphEdges(mainGraph)

		// update all graphs every five seconds
		Tasks.syncRepeat(0L, 100L) {
			Bukkit.getOnlinePlayers().forEach {

			}
		}
	}

	override fun onDisable() {
		for (player in playerGraphs.keys) {
			deleteGraph(playerGraphs[player])
			playerGraphs.remove(player)
		}
		deleteGraph(mainGraph)
	}

	private fun populateGraphVertices(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
		// add all planets as vertices to graph
		for (planet in Space.getPlanets()) {
			val wp = WaypointVertex(
				name = planet.name,
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
			mainGraph.addVertex(wpEntry)
			mainGraph.addVertex(wpExit)
		}
	}

	private fun populateGraphEdges(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>) {
		// add edges for each vertex
		for (vertex in graph.vertexSet()) {
			// connect vertices that are in the same space world (and not itself) (celestials in the same world)
			for (otherVertex in graph.vertexSet()) {
				if (vertex == otherVertex) continue
				if (vertex.loc.world == otherVertex.loc.world) {
					val edge = graph.addEdge(vertex, otherVertex)
					graph.setEdgeWeight(edge, vertex.loc.distance(otherVertex.loc))
				}
			}

			// add edges between vertices linked to another (i.e. beacons)
			if (vertex.linkedWaypoint != null) {
				val edge = graph.addEdge(vertex, vertex.linkedWaypoint)
				edge.hyperspaceEdge = true
				graph.setEdgeWeight(edge, Hyperspace.INTER_SYSTEM_DISTANCE.toDouble())
			}
		}
	}

	fun getVertex(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>, name: String): WaypointVertex? {
		return graph.vertexSet().find { it.name == name }
	}

	private fun deleteGraph(graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>?) {
		if (graph != null) {
			graph.removeAllEdges(graph.edgeSet())
			graph.removeAllVertices(graph.vertexSet())
		}
	}

	fun reloadMainGraph() {
		if (!GraphTests.isEmpty(mainGraph)) {
			deleteGraph(mainGraph)
		}
		populateGraphVertices(mainGraph)
		populateGraphEdges(mainGraph)
	}

	fun printMainGraphVertices(player: Player) {
		for (vertex in mainGraph.vertexSet()) {
			player.information(
				StringBuilder(vertex.name)
					.append(" at ${vertex.loc}")
					.append(" with companion vertex ${vertex.linkedWaypoint?.name}")
					.toString()
			)
		}
	}

	fun printMainGraphEdges(player: Player) {
		for (edge in mainGraph.edgeSet()) {
			player.information(
				StringBuilder("Edge from ")
					.append(mainGraph.getEdgeSource(edge).name)
					.append(" -> ")
					.append(mainGraph.getEdgeTarget(edge).name)
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

	@Suppress("unused")
	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val playerGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
		playerGraphs[event.player.uniqueId] = playerGraph
	}

	@Suppress("unused")
	@EventHandler
	fun onPlayerLeave(event: PlayerQuitEvent) {
		val playerGraph = playerGraphs[event.player.uniqueId] ?: return
		deleteGraph(playerGraph)
		playerGraphs.remove(event.player.uniqueId)
	}
}

data class WaypointVertex(val name: String, val loc: Location, var linkedWaypoint: WaypointVertex? = null)

data class WaypointEdge(var hyperspaceEdge: Boolean = false) : DefaultWeightedEdge()
