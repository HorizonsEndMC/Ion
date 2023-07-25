package net.horizonsend.ion.server.miscellaneous.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.starlegacy.command.SLCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("ion")
@CommandPermission("ion.utilities")
object IonCommand : SLCommand() {
	val debugEnabledPlayers = mutableListOf<UUID>()

	@Suppress("Unused")
	@Subcommand("view set")
	fun setServerViewDistance(sender: CommandSender, renderDistance: Int) {
		if (renderDistance > 32) {
			sender.userError("View distances above 32 are not supported.")
			return
		}

		if (renderDistance < 2) {
			sender.userError("View distances below 2 are not supported.")
			return
		}

		for (world in Bukkit.getWorlds()) {
			world.viewDistance = renderDistance
		}

		sender.sendMessage("View distance set to $renderDistance.")
	}

	@Suppress("Unused")
	@Subcommand("simulation set")
	fun setServerSimulationDistance(sender: CommandSender, simulationDistance: Int) {
		if (simulationDistance > 32) {
			sender.userError("Simulation distances above 32 are not supported.")
			return
		}

		if (simulationDistance < 2) {
			sender.userError("Simulation distances below 2 are not supported.")
			return
		}

		for (world in Bukkit.getWorlds()) {
			world.viewDistance = simulationDistance
		}

		sender.sendMessage("Simulation distance set to $simulationDistance.")
	}

	@Suppress("Unused")
	@Subcommand("view get")
	fun getServerViewDistance(sender: CommandSender) {
		sender.sendMessage("View distance is currently set to ${Bukkit.getWorlds()[0].viewDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("simulation get")
	fun getServerSimulationDistance(sender: CommandSender) {
		sender.sendMessage("Simulation distance is currently set to ${Bukkit.getWorlds()[0].simulationDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("debug")
	fun debugToggle(sender: Player) {
		if (debugEnabledPlayers.contains(sender.uniqueId)) {
			debugEnabledPlayers.remove(sender.uniqueId)
			sender.success("Disabled debug mode")
		} else {
			debugEnabledPlayers.add(sender.uniqueId)
			sender.success("Enabled debug mode")
		}
	}
}

fun Player.debugBanner(message: String) = debug("------------------- $message -------------------")
fun Player.debug(message: String) =
	if (IonCommand.debugEnabledPlayers.contains(uniqueId)) {
		information(message)
	} else null
fun Player.debugRed(message: String) =
	if (IonCommand.debugEnabledPlayers.contains(uniqueId)) {
		serverError(message)
	} else null
