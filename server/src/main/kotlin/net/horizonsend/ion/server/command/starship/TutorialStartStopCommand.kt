package net.horizonsend.ion.server.command.starship

import net.horizonsend.ion.server.command.SLCommand

object TutorialStartStopCommand : SLCommand() {
//	@Suppress("Unused") TODO
//	@CommandAlias("tutorialstart")
//	@CommandPermission("tutorial.start")
//	fun onTutorialStart(sender: CommandSender, @Optional player: OnlinePlayer?) {
//		val targetPlayer: Player = when {
//			player != null -> player.getPlayer()
//			sender is Player -> sender
//			sender is BlockCommandSender -> {
//				val block = sender.block
//				val location = block.location
//				block.world.getNearbyPlayers(location, 100.0, 100.0, 100.0)
//					.sortedBy { it.location.distance(location) }
//					.firstOrNull() ?: fail { "No nearby players" }
//			}
//
//			else -> fail { "Specify a player" }
//		}
//
//		try {
//			Tutorials.start(targetPlayer)
//		} catch (e: Throwable) {
//			sender.serverError("There was an error starting the tutorial, please contact an admin.")
//		}
//	}
//
//	@Suppress("Unused")
//	@CommandAlias("tutorialstop")
//	@CommandPermission("tutorial.stop")
//	fun onTutorialStop(sender: CommandSender, player: OnlinePlayer) {
//		Tutorials.stop(player.getPlayer())
//	}
//
//	@Suppress("Unused")
//	@Subcommand("exit|quit")
//	@CommandAlias("tutorialexit|tutorialquit|quittutorial|exittutorial")
//	fun onTutorialExit(sender: Player) {
//		Tutorials.stop(sender)
//	}
}
