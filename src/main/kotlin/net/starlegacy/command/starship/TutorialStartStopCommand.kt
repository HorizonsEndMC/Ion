package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.starlegacy.command.SLCommand
import net.starlegacy.feature.tutorial.TutorialManager
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TutorialStartStopCommand : SLCommand() {
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

		TutorialManager.start(targetPlayer)
	}

	@CommandAlias("tutorialstop")
	@CommandPermission("tutorial.stop")
	fun onTutorialStop(sender: CommandSender, player: OnlinePlayer) {
		TutorialManager.stop(player.getPlayer())
	}

	@Subcommand("exit|quit")
	@CommandAlias("tutorialexit|tutorialquit|quittutorial|exittutorial")
	fun onTutorialExit(sender: Player) {
		TutorialManager.stop(sender)
	}
}
