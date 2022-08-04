package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("achievements")
class AchievementsCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onAchievementsList(sender: Player, @Optional target: String?) {

	}

	@Suppress("Unused")
	@Subcommand("grant")
	@CommandCompletion("@players")
	@CommandPermission("ion.achievements.grant")
	fun onAchievementGrant(sender: CommandSender, target: String) {

	}

	@Suppress("Unused")
	@Subcommand("revoke")
	@CommandCompletion("@players")
	@CommandPermission("ion.achievements.revoke")
	fun onAchievementRevoke(sender: CommandSender, target: String) {

	}
}