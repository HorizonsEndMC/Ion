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
	fun onReloadMainMap(
		sender: Player
	) {
		WaypointManager.reloadMainGraph()
		sender.success("Main map reloaded")
	}

	@Suppress("unused")
	@Subcommand("test")
	@CommandCompletion("vertex|edge")
	fun onTestMainMap(
		sender: Player,
		option: String
	) {
		when (option) {
			"vertex" -> {
				WaypointManager.printMainGraphVertices(sender)
			}
			"edge" -> {
				WaypointManager.printMainGraphEdges(sender)
			}
			else -> {
				throw InvalidCommandArgument("Invalid choice; select vertex/edge")
			}
		}
	}
}
