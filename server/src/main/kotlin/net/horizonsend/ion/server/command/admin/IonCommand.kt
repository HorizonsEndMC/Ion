package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("ion")
@CommandPermission("ion.utilities")
object IonCommand : SLCommand() {
	val debugEnabledPlayers = mutableListOf<Audience>()

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

		sender.success("View distance set to $renderDistance.")
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

		sender.success("Simulation distance set to $simulationDistance.")
	}

	@Suppress("Unused")
	@Subcommand("view get")
	fun getServerViewDistance(sender: CommandSender) {
		sender.information("View distance is currently set to ${Bukkit.getWorlds()[0].viewDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("simulation get")
	fun getServerSimulationDistance(sender: CommandSender) {
		sender.information("Simulation distance is currently set to ${Bukkit.getWorlds()[0].simulationDistance}.")
	}

	@Suppress("Unused")
	@Subcommand("debug")
	fun debugToggle(sender: Player) {
		if (debugEnabledPlayers.contains(sender)) {
			debugEnabledPlayers.remove(sender)
			sender.success("Disabled debug mode")
		} else {
			debugEnabledPlayers.add(sender)
			sender.success("Enabled debug mode")
		}
	}
}

fun Audience.debugBanner(message: String) = debug("------------------- $message -------------------")
fun Audience.debug(message: String) {
	if (this !is Player) return

	if (IonCommand.debugEnabledPlayers.contains(this)) {
		information(message)
	}
}

fun Audience.debugRed(message: String) {
	if (this !is Player) return

	if (IonCommand.debugEnabledPlayers.contains(this)) {
		serverError(message)
	}
}
