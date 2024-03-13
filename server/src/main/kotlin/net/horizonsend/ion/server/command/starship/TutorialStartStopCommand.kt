package net.horizonsend.ion.server.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.tutorial.TutorialManager
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TutorialStartStopCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("tutorialstart")
	@CommandPermission("tutorial.start")
	fun onTutorialStart(sender: CommandSender, @Optional player: OnlinePlayer?) {
		val targetPlayer: Player = when {
			player != null -> player.getPlayer()
			sender is Player -> sender
			sender is BlockCommandSender -> {
				val block = sender.block
				val location = block.location
				block.world.getNearbyPlayers(location, 100.0, 100.0, 100.0)
					.sortedBy { it.location.distance(location) }
					.firstOrNull() ?: fail { "No nearby players" }
			}

			else -> fail { "Specify a player" }
		}

		try {
			TutorialManager.start(targetPlayer)
		} catch (e: Throwable) {
			sender.serverError("There was an error starting the tutorial, please contact an admin.")
		}
	}

	@Suppress("Unused")
	@CommandAlias("tutorialstop")
	@CommandPermission("tutorial.stop")
	fun onTutorialStop(sender: CommandSender, player: OnlinePlayer) {
		TutorialManager.stop(player.getPlayer())
	}

	@Suppress("Unused")
	@Subcommand("exit|quit")
	@CommandAlias("tutorialexit|tutorialquit|quittutorial|exittutorial")
	fun onTutorialExit(sender: Player) {
		TutorialManager.stop(sender)
	}
}
