package net.horizonsend.ion.server.features.waypoint.command

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.litote.kmongo.set
import org.litote.kmongo.setTo

@CommandAlias("waypoint")
object WaypointCommand : SLCommand() {
    // add vertex as destination
    @Suppress("unused")
    @CommandAlias("add")
    @CommandCompletion("@planets|@hyperspaceGates")
    @Description("Add a waypoint to the route navigation")
    fun onSetWaypoint(
        sender: Player,
        option: String
    ) {
        val vertex = WaypointManager.getVertex(WaypointManager.mainGraph, option)
        if (vertex == null) {
            sender.userError("Vertex not found")
            return
        }
        if (WaypointManager.addDestination(sender, vertex)) {
            WaypointManager.updatePlayerGraph(sender)
            WaypointManager.updatePlayerPaths(sender)
            WaypointManager.updateNumJumps(sender)
            sender.success("Vertex ${vertex.name} added")
        } else {
            sender.userError("Too many destinations added (maximum of ${WaypointManager.MAX_DESTINATIONS})")
        }
    }

    @Suppress("unused")
    @CommandAlias("add")
    @CommandCompletion("world|x|z")
    @Description("Add a waypoint to the route navigation")
    fun onSetWaypoint(
        sender: Player,
        world: String,
        xCoordinate: String,
        zCoordinate: String
    ) {
        val getWorld = Bukkit.getWorld(world)
        if (getWorld == null) {
            sender.userError("Entered world does not exist")
            return
        }
        if (!SpaceWorlds.contains(getWorld)) {
            sender.userError("World is not a space world")
            return
        }

        val x = MiscStarshipCommands.parseNumber(xCoordinate, sender.location.x.toInt()).toDouble()
        val z = MiscStarshipCommands.parseNumber(zCoordinate, sender.location.z.toInt()).toDouble()
        val vertex = WaypointManager.addTempVertex(sender, Location(getWorld, x, 128.0, z))
        if (vertex == null) {
            sender.serverError("Failed to generate waypoint in empty space")
        }

        if (WaypointManager.addDestination(sender, vertex!!)) {
            WaypointManager.updatePlayerGraph(sender)
            WaypointManager.updatePlayerPaths(sender)
            WaypointManager.updateNumJumps(sender)
            sender.success("Vertex ${vertex.name} added")
        } else {
            sender.userError("Too many destinations added (maximum of ${WaypointManager.MAX_DESTINATIONS})")
        }
    }

    // clear all vertices from destinations
    @Suppress("unused")
    @CommandAlias("clear")
    @Description("Remove all waypoints from the route navigation")
    fun onClearWaypoint(
        sender: Player
    ) {
        if (!WaypointManager.playerDestinations[sender.uniqueId].isNullOrEmpty()) {
            WaypointManager.playerDestinations[sender.uniqueId]?.clear()
            WaypointManager.playerPaths.remove(sender.uniqueId)
            WaypointManager.playerTempWaypoints.remove(sender.uniqueId)
            WaypointManager.playerNumJumps.remove(sender.uniqueId)
            sender.success("All waypoints cleared")
            return
        } else {
            sender.userError("No waypoints to remove")
            return
        }
    }

    // pop last vertex from destinations
    @Suppress("unused")
    @CommandAlias("undo")
    @Description("Remove the last waypoint added to the route navigation")
    fun onUndoWaypoint(
        sender: Player
    ) {
        if (!WaypointManager.playerDestinations[sender.uniqueId].isNullOrEmpty()) {
            val vertex = WaypointManager.playerDestinations[sender.uniqueId]?.last()
            WaypointManager.playerDestinations[sender.uniqueId]?.removeLast()
            WaypointManager.updatePlayerGraph(sender)
            WaypointManager.updatePlayerPaths(sender)
            WaypointManager.updateNumJumps(sender)
            sender.success("Last waypoint ${vertex?.name} removed")
            return
        } else {
            sender.userError("No waypoints to remove")
            return
        }
    }

    // reload graphs with new vertices
    @Suppress("unused")
    @Subcommand("reload")
    @CommandCompletion("main|player")
    @CommandPermission("waypoint.reload")
    @Description("DEBUG: Reloads the main map's vertices and edges")
    fun onReloadMainMap(
        sender: Player,
        mapType: String
    ) {
        when (mapType) {
            "main" -> {
                WaypointManager.reloadMainGraph()
                sender.success("Main map reloaded")
            }

            "player" -> {
                WaypointManager.updatePlayerGraph(sender)
                sender.success("Player map reloaded")
            }
        }
    }

    // print status of main graph
    @Suppress("unused")
    @Subcommand("main")
    @CommandCompletion("vertex|edge")
    @CommandPermission("waypoint.print")
    @Description("DEBUG: Prints the vertices or edges of the main map")
    fun onTestMainMap(
        sender: Player,
        option: String
    ) {
        when (option) {
            "vertex" -> {
                WaypointManager.printGraphVertices(WaypointManager.mainGraph, sender)
            }

            "edge" -> {
                WaypointManager.printGraphEdges(WaypointManager.mainGraph, sender)
            }

            else -> {
                throw InvalidCommandArgument("Invalid choice; select vertex/edge")
            }
        }
    }

    // print status of player's own graph
    @Suppress("unused")
    @Subcommand("player")
    @CommandCompletion("vertex|edge")
    @CommandPermission("waypoint.print")
    @Description("DEBUG: Prints the vertices or edges of the player's own map")
    fun onTestPlayerMap(
        sender: Player,
        option: String
    ) {
        when (option) {
            "vertex" -> {
                WaypointManager.printGraphVertices(WaypointManager.playerGraphs[sender.uniqueId], sender)
            }

            "edge" -> {
                WaypointManager.printGraphEdges(WaypointManager.playerGraphs[sender.uniqueId], sender)
            }

            else -> {
                throw InvalidCommandArgument("Invalid choice; select vertex/edge")
            }
        }
    }

    // gets a celestial object and outputs waypoint data
    @Suppress("unused")
    @Subcommand("get")
    @CommandCompletion("@planets|@hyperspaceGates")
    @CommandPermission("waypoint.print")
    @Description("DEBUG: Prints information of a vertex")
    fun onGetVertex(
        sender: Player,
        option: String
    ) {
        val vertex = WaypointManager.getVertex(WaypointManager.mainGraph, option)
        if (vertex == null) {
            sender.userError("Vertex not found")
        } else {
            sender.information(
                "Vertex ${vertex.name} at ${vertex.loc.x}, ${vertex.loc.y}, ${vertex.loc.z} in world " +
                        "${vertex.loc.world} with linked vertex ${vertex.linkedWaypoint}"
            )
        }
    }

    // calculates shortest path with a player's destinations
    @Suppress("unused")
    @Subcommand("path get")
    @CommandPermission("waypoint.reload")
    @Description("DEBUG: Calculates the shortest path of a route manually")
    fun onGetPath(
        sender: Player
    ) {
        val paths = WaypointManager.findShortestPath(sender)
        if (paths == null) {
            // path could not be calculated (a vertex is completely separated from the graph)
            sender.userError("No connections can be found to get to the destination")
            return
        } else if (paths.isEmpty()) {
            // paths exists but is empty; implies that prerequisite conditions were not met
            sender.userError("No waypoints set")
        }
        // update path list
        WaypointManager.playerPaths[sender.uniqueId] = paths
    }

    @Suppress("unused")
    @Subcommand("path")
    @Description("Prints detailed information of all waypoints on a navigation route")
    fun onPrintPath(
        sender: Player
    ) {
        val paths = WaypointManager.playerPaths[sender.uniqueId]
        if (paths.isNullOrEmpty()) {
            sender.userError("Route is empty")
            return
        }
        for ((i, path) in paths.withIndex()) {
            sender.information("${i + 1}: ${path.startVertex.name} to ${path.endVertex.name} " +
                    "with total distance ${path.weight}"
            )
            for ((j, edge) in path.edgeList.withIndex()) {
                sender.information("    ${i + 1}.$j: ${edge.source.name} to ${edge.target.name} " +
                        "with distance ${WaypointManager.playerGraphs[sender.uniqueId]!!.getEdgeWeight(edge)}"
                )
            }
        }
    }

    @Suppress("unused")
    @Subcommand("compactWaypoints")
    @Description("Toggles compact waypoints; intermediate jumps are not displayed during navigation")
    fun onToggleCompactWaypoints(
        sender: Player,
        @Optional toggle: Boolean?
    ) {
        val waypointsCompactWaypoints = toggle ?: !PlayerCache[sender].compactWaypoints
        SLPlayer.updateById(sender.slPlayerId, set(SLPlayer::compactWaypoints setTo waypointsCompactWaypoints))
        sender.success("Changed compact waypoints visibility to $waypointsCompactWaypoints")
    }

    @Suppress("unused")
    @Subcommand("jumps")
    @CommandPermission("waypoint.print")
    @Description("DEBUG: Gets number of jumps manually")
    fun onGetNumJumps(
        sender: Player
    ) {
        val jumps = WaypointManager.playerNumJumps[sender.uniqueId]
        sender.information("Number of jumps: $jumps")
    }

    @Suppress("unused")
    @Subcommand("string")
    @CommandPermission("waypoint.print")
    @Description("DEBUG: Gets the route string manually")
    fun onGetRouteString(
        sender: Player
    ) {
        sender.sendMessage(Component.text(WaypointManager.getRouteString(sender))
            .font(Key.key("horizonsend:sidebar"))
        )
    }
}
