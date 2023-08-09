package net.horizonsend.ion.server.features.waypoint.command

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import org.bukkit.entity.Player

@CommandAlias("waypoint")
object WaypointCommand : SLCommand() {
    @Suppress("unused")
    @CommandAlias("set")
    @CommandCompletion("@planets|@hyperspaceGates")
    fun onSetWaypoint(
        sender: Player,
        option: String
    ) {
        val vertex = WaypointManager.getVertex(WaypointManager.mainGraph, option)
        if (vertex == null) {
            sender.userError("Vertex not found")
            return
        }

        if (WaypointManager.playerDestinations[sender.uniqueId].isNullOrEmpty()) {
            WaypointManager.playerDestinations[sender.uniqueId] = mutableListOf(vertex)
            sender.success("Waypoint to ${vertex.name} set")
            return
        } else if (WaypointManager.playerDestinations[sender.uniqueId]!!.size >= 5) {
            sender.userError(("Too many waypoints set"))
            return
        } else {
            WaypointManager.playerDestinations[sender.uniqueId]!!.add(vertex)
            sender.success("Waypoint to ${vertex.name} set")
            return
        }
    }

    @Suppress("unused")
    @CommandAlias("clear")
    fun onClearWaypoint(
        sender: Player
    ) {
        if (!WaypointManager.playerDestinations[sender.uniqueId].isNullOrEmpty()) {
            WaypointManager.playerDestinations[sender.uniqueId]?.clear()
            sender.success("All waypoints cleared")
            return
        } else {
            sender.userError("No waypoints to remove")
            return
        }
    }

    @Suppress("unused")
    @CommandAlias("undo")
    fun onUndoWaypoint(
        sender: Player
    ) {
        if (!WaypointManager.playerDestinations[sender.uniqueId].isNullOrEmpty()) {
            val vertex = WaypointManager.playerDestinations[sender.uniqueId]?.last()
            WaypointManager.playerDestinations[sender.uniqueId]?.removeLast()
            sender.success("Last waypoint ${vertex?.name} removed")
            return
        } else {
            sender.userError("No waypoints to remove")
            return
        }
    }

    @Suppress("unused")
    @Subcommand("reload")
    @CommandCompletion("main|player")
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

    @Suppress("unused")
    @Subcommand("main")
    @CommandCompletion("vertex|edge")
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

    @Suppress("unused")
    @Subcommand("player")
    @CommandCompletion("vertex|edge")
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

    @Suppress("unused")
    @Subcommand("get")
    @CommandCompletion("@planets|@hyperspaceGates")
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
    @Subcommand("path")
    fun onGetVertex(
        sender: Player
    ) {
        val paths = WaypointManager.findShortestPath(sender)
        if (paths.isEmpty()) {
            return
        }
        for (path in paths) {
            sender.information("${path.startVertex.name} to ${path.endVertex.name} with total weight ${path.weight}")
            for (edge in path.edgeList) {
                sender.information("  ${edge.source.name} to ${edge.target.name} with weight ${WaypointManager.playerGraphs[sender.uniqueId]!!.getEdgeWeight(edge)}")
            }
        }
    }
}
