package net.horizonsend.ion.server.features.waypoint.command

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.starship.MiscStarshipCommands
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

@CommandAlias("route")
object WaypointCommand : SLCommand() {
    @Suppress("unused")
    @CommandAlias("add")
    @Description("Add a waypoint to the route navigation")
    fun onSetWaypoint(
        sender: Player
    ) {
        sender.userError("Usage: /waypoint add <planet/hyperspaceBeacon> or /waypoint add <spaceWorld> <x> <z>")
    }

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
        val vertex = WaypointManager.addTempVertex(Location(getWorld, x, 128.0, z))

        if (WaypointManager.addDestination(sender, vertex)) {
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

    @Suppress("unused")
    @Subcommand("list")
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
                    "with total distance ${path.weight.toInt()}"
            )
            for ((j, edge) in path.edgeList.withIndex()) {
                sender.information("    ${i + 1}.${j + 1}: ${edge.source.name} to ${edge.target.name} " +
                        "with distance ${WaypointManager.playerGraphs[sender.uniqueId]!!.getEdgeWeight(edge).toInt()}"
                )
            }
        }
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
