package net.horizonsend.ion.server

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.miscellaneous.extensions.userError
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("ion")
@CommandPermission("ion.utilities")
class IonCommand : BaseCommand() {
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
}
