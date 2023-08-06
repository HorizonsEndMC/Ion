package net.horizonsend.ion.server.features.waypoint.command

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.waypoint.WaypointManager
import org.bukkit.entity.Player

@CommandAlias("waypoint")
object WaypointCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun defaultCase(
		sender: Player
	) {
		sender.userError("Usage: Figure this out later")
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
}
