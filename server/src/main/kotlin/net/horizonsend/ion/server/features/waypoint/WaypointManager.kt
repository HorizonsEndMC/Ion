package net.horizonsend.ion.server.features.waypoint

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.repeatString
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.sidebar.SidebarIcon
import net.horizonsend.ion.server.features.sidebar.command.BookmarkCommand
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.body.CachedStar
import net.horizonsend.ion.server.features.space.body.CelestialBody
import net.horizonsend.ion.server.features.space.body.planet.CachedPlanet
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.event.StarshipPilotedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipUnpilotedEvent
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.listen
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.jgrapht.GraphPath
import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.util.UUID
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sqrt

object WaypointManager : IonServerComponent() {
    // mainGraph holds the server graph
    val mainGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)

    // playerGraphs hold copies of mainGraph, with additional vertices per player (for shortest path calculation)
    val playerGraphs: MutableMap<UUID, SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>> = mutableMapOf()
    val playerDestinations: MutableMap<UUID, MutableList<WaypointVertex>> = mutableMapOf()
    val playerPaths: MutableMap<UUID, List<GraphPath<WaypointVertex, WaypointEdge>>> = mutableMapOf()
    val playerNumJumps: MutableMap<UUID, Int> = mutableMapOf()

    const val MAX_DESTINATIONS = 5
    private const val WAYPOINT_REACHED_DISTANCE = 500
    private const val MAX_ROUTE_SEGMENTS = 50

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
                updatePlayerGraph(player)
                if (playerDestinations.isNotEmpty()) {
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
			event.starship.playerPilot?.let {
				updatePlayerGraph(it)
				updatePlayerPaths(it)
				updateNumJumps(it)
			}
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
        return graph.vertexSet().find { it.name.equals(name, ignoreCase = true) }
    }

    private fun connectVerticesInSameWorld(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        vertex: WaypointVertex
    ) {
		if (vertex.well) return //dont add outgoing edges from planets

        // find vertices that are in the same world as the current vertex
        val verticesSameWorld = graph.vertexSet()
            .filter { otherVertex -> otherVertex.loc.world == vertex.loc.world && vertex != otherVertex }
        // create edges
        for (otherVertex in verticesSameWorld) {
			//dont make edges that pass though gravwells
			if (!validateEdge(vertex,otherVertex)) continue
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
			if (otherVertex.well) continue //dont add outgoing edges from planets
            graph.addEdge(otherVertex, vertex, inEdge)
            graph.setEdgeWeight(inEdge, otherVertex.loc.distance(vertex.loc))
        }
    }

	private fun validateEdge(vertex: WaypointVertex, otherVertex: WaypointVertex) : Boolean {
		val planets = Space.getAllPlanets(vertex.loc.world)
		val suns = Space.getStars(vertex.loc.world)
		if (!suns.all{ pathIntersectingWell(vertex,otherVertex,it, it.name) }) return false
		return planets.all{ pathIntersectingWell(vertex,otherVertex,it, it.name) }
	}

	/** checks if a given path collides with a gravity well and is unvalid
	 * A path is only invalid if the well is inbetween the two points
	 * and it is not a cage vertex connecting to its parent*/
	private fun pathIntersectingWell(vertex : WaypointVertex, otherVertex: WaypointVertex, body : CelestialBody, name : String): Boolean {
		if (vertex.name.contains("cage") xor otherVertex.name.contains("cage")) {
			if (vertex.name.contains(name) and otherVertex.name.contains(name)) return false
		}

		val p1 = vertex.loc.toVector()
		val p2 = otherVertex.loc.toVector()
		val center = body.location.toCenterVector()
		val radius = when (body) {
			is CachedPlanet -> MassShadows.PLANET_RADIUS
			is CachedStar -> MassShadows.STAR_RADIUS
			else -> MassShadows.PLANET_RADIUS}

		// Check if either endpoint is inside the circle
		if (p1.distance(center) <= radius || p2.distance(center) <= radius) {
			return true
		}

		// Vector form of segment
		val d = p2.clone().subtract(p1)  // Direction vector
		val f = p1.clone().subtract(center) // Vector from circle center to p1
		d.y = 0.0
		f.y = 0.0

		val a = d.dot(d)
		val b = 2 * f.dot(d)
		val c = f.dot(f) - radius * radius

		// Quadratic formula discriminant
		val discriminant = b * b - 4 * a * c

		if (discriminant < 0) {
			return true  // No intersection
		}

		// Solve quadratic equation for t
		val sqrtD = sqrt(discriminant)
		val t1 = (-b - sqrtD) / (2 * a)
		val t2 = (-b + sqrtD) / (2 * a)

		// Check if the intersection occurs within the segment (0 <= t <= 1)
		//if both points of intersection are in the line segment then its an invalid path
		return !((t1 in 0.0..1.0) && (t2 in 0.0..1.0))
	}

    /**
     * mainGraph-specific functions
     */
    private fun populateMainGraphVertices() {
        // add all planets as vertices to mainGraph
        for (planet in Space.getAllPlanets()) {
            val vertex = WaypointVertex(
                name = planet.name,
                icon = SidebarIcon.PLANET_ICON.text.first(),
                loc = planet.location.toLocation(planet.spaceWorld!!),
				well = true
            )
            mainGraph.addVertex(vertex)
			addCageVertices(planet.name,planet,1.25,5)
			addCageVertices(planet.name,planet,2.5,10)
        }
		for (star in Space.getStars()) {
			addCageVertices(star.name,star,1.25,5)
			addCageVertices(star.name,star,2.5,10)
		}

        // add all beacons as vertices to mainGraph
        for (beacon in ConfigurationFiles.serverConfiguration().beacons) {
            // 2 vertices for each beacon's entry and exit point
            val vertexEntry = WaypointVertex(
                name = beacon.name.replace(" ", "_"),
                icon = SidebarIcon.HYPERSPACE_BEACON_ENTER_ICON.text.first(),
                loc = beacon.spaceLocation.toLocation()
            )
            val vertexExit = WaypointVertex(
                name = StringBuilder(beacon.name.replace(" ", "_")).append("_Exit").toString(),
                icon = SidebarIcon.HYPERSPACE_BEACON_EXIT_ICON.text.first(),
                loc = beacon.destination.toLocation()
            )
            // link edge vertex with exit vertex (for edge connections later)
            vertexEntry.linkedWaypoint = vertexExit.name
            mainGraph.addVertex(vertexEntry)
            mainGraph.addVertex(vertexExit)
        }
    }

	private fun addCageVertices(name: String, body : CelestialBody, rMod : Double , n : Int) {
		val center = body.location
		val r = when (body) {
			is CachedPlanet -> MassShadows.PLANET_RADIUS
			is CachedStar -> MassShadows.STAR_RADIUS
			else -> MassShadows.PLANET_RADIUS
		} * rMod
		for (i in 0 until n) {
			val theta = (2.0 * PI / n * i)
			val vec = Vector(0.0,0.0,1.0).multiply(r)
			vec.rotateAroundY(theta)
			val pos = center.toVector().add(vec)
			if (MassShadows.find(body.spaceWorld!!, pos.x, pos.z) != null ) continue //dont add points inside gravity wells
			val formatedNum = String.format("%.2f",rMod)
			val vertex = WaypointVertex(
				name = "${name}_cage_${formatedNum}_${i}",
				icon = SidebarIcon.ROUTE_SEGMENT_ICON.text.first(),
				loc = pos.toLocation(body.spaceWorld!!),
				linkedWaypoint = null,
				hidden = true
			)
			mainGraph.addVertex(vertex)
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
                populatePlayerBookmarkVertex(playerGraph, player)
            }
        } else {
            // add player's graph to the map
            val playerGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
            clonePlayerGraphFromMain(playerGraph)
            updatePlayerPositionVertex(playerGraph, player)
            populatePlayerBookmarkVertex(playerGraph, player)
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
            icon = SidebarIcon.PLUS_CROSS_ICON.text.first(),
            loc = player.location,
            linkedWaypoint = null
        )
        graph.addVertex(newVertex)
        connectVerticesInSameWorld(graph, newVertex)
    }

    private fun populatePlayerBookmarkVertex(
        graph: SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>,
        player: Player
    ) {
        val bookmarks = BookmarkCommand.getBookmarks(player)

        // Bookmarks may share the same name as a planet/gate/other object, but because those are loaded first,
        // getVertex() will default to the object instead of the bookmark
        for (bookmark in bookmarks) {
            val world = Bukkit.getWorld(bookmark.worldName)
            if (world == null || !world.hasFlag(WorldFlag.SPACE_WORLD)) continue
            val newVertex = WaypointVertex(
                name = bookmark.name,
                icon = SidebarIcon.BOOKMARK_ICON.text.first(),
                loc = Location(world, bookmark.x.toDouble(), bookmark.y.toDouble(), bookmark.z.toDouble()),
                linkedWaypoint = null
            )
            graph.addVertex(newVertex)
            connectVerticesInSameWorld(graph, newVertex)
        }
    }

    fun addTempVertex(loc: Location): WaypointVertex {
        return WaypointVertex(
            name = "Waypoint @ ${loc.world.name} (${loc.x.toInt()}, ${loc.z.toInt()})",
            icon = SidebarIcon.PLUS_CROSS_ICON.text.first(),
            loc = loc,
            linkedWaypoint = null
        )
    }

    /**
     * destination and path functions
     */
    fun addDestination(player: Player, vertex: WaypointVertex): Boolean {
        return if (playerDestinations[player.uniqueId].isNullOrEmpty()) {
            // list not created
            playerDestinations[player.uniqueId] = mutableListOf(vertex)
            playerGraphs[player.uniqueId]!!.addVertex(vertex)
            connectVerticesInSameWorld(playerGraphs[player.uniqueId]!!, vertex)
            true
        } else if (playerDestinations[player.uniqueId]!!.size >= MAX_DESTINATIONS) {
            // list is full
            false
        } else {
            // list exists
            playerDestinations[player.uniqueId]!!.add(vertex)
            playerGraphs[player.uniqueId]!!.addVertex(vertex)
            connectVerticesInSameWorld(playerGraphs[player.uniqueId]!!, vertex)
            true
        }
    }

    private fun findShortestPath(player: Player): List<GraphPath<WaypointVertex, WaypointEdge>>? {
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

	/**
	 * Helper function for finding a distance between two locations in space
	 */
	fun findShortestPathBetweenLocations(loc1: Location, loc2: Location): GraphPath<WaypointVertex, WaypointEdge>? {
		val tempGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
		clonePlayerGraphFromMain(tempGraph)

		val loc1Vertex = WaypointVertex(
			name = "Location 1",
			icon = SidebarIcon.PLUS_CROSS_ICON.text.first(),
			loc = loc1,
			linkedWaypoint = null
		)
		tempGraph.addVertex(loc1Vertex)
		connectVerticesInSameWorld(tempGraph, loc1Vertex)

		val loc2Vertex = WaypointVertex(
			name = "Location 2",
			icon = SidebarIcon.PLUS_CROSS_ICON.text.first(),
			loc = loc2,
			linkedWaypoint = null
		)
		tempGraph.addVertex(loc2Vertex)
		connectVerticesInSameWorld(tempGraph, loc2Vertex)

		return DijkstraShortestPath.findPathBetween(tempGraph, loc1Vertex, loc2Vertex)
	}

	/**
	 * Helper function for finding a distance between two locations in space
	 */
	fun findShortestPathToPlanet(source: Location, world: World): GraphPath<WaypointVertex, WaypointEdge>? {
		val tempGraph = SimpleDirectedWeightedGraph<WaypointVertex, WaypointEdge>(WaypointEdge::class.java)
		clonePlayerGraphFromMain(tempGraph)

		val loc1Vertex = WaypointVertex(
			name = "Location 1",
			icon = SidebarIcon.PLUS_CROSS_ICON.text.first(),
			loc = source,
			linkedWaypoint = null
		)
		tempGraph.addVertex(loc1Vertex)
		connectVerticesInSameWorld(tempGraph, loc1Vertex)

		val planetVertex = getVertex(mainGraph,Space.getPlanet(world)!!.name)

		return DijkstraShortestPath.findPathBetween(tempGraph, loc1Vertex, planetVertex)
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

                if (!mainGraph.containsVertex(vertex)) {
                    // vertex was temporary; remove entirely from player graph
                    playerGraphs[player.uniqueId]?.removeVertex(vertex)
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

        val maxRange = (navComp.multiblock.baseRange * starship.balancing.hyperspaceRangeMultiplier)

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
                    str.append(repeatString(SidebarIcon.ROUTE_SEGMENT_ICON.text, (jumps - 1).coerceAtMost(MAX_ROUTE_SEGMENTS)))
                }
                str.append(edge.target.icon)
            }
        }
        return str.toString()
    }

    fun getNextWaypoint(player: Player): String? {
        val playerPath = playerPaths[player.uniqueId] ?: return null
        if (playerPath.isEmpty()) return null
        return playerPath.first().edgeList.first().target.name
    }

    fun getLastWaypoint(player: Player): String? {
        val playerPath = playerPaths[player.uniqueId] ?: return null
        if (playerPath.isEmpty()) return null
        return playerPath.last().endVertex.name
    }
}

/**
 * data classes
 */
data class WaypointVertex(
    val name: String,
    val icon: Char,
    var loc: Location,
    var linkedWaypoint: String? = null,
	val hidden : Boolean = false,
	val well : Boolean = false
)

data class WaypointEdge(
    val source: WaypointVertex,
    val target: WaypointVertex,
    var hyperspaceEdge: Boolean = false
) : DefaultWeightedEdge()
