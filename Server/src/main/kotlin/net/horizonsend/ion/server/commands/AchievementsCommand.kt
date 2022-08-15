package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.managers.ScreenManager.openScreen
import net.horizonsend.ion.server.screens.AchievementsScreen
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

@CommandAlias("achievements")
class AchievementsCommand : BaseCommand() {
	@Default
	@Suppress("Unused")
	fun onAchievementsList(sender: Player) {
		sender.openScreen(AchievementsScreen(sender.name))
	}

	@Suppress("Unused")
	@Default
	fun onAchievementsList(sender: Player, target: String) {
		sender.openScreen(AchievementsScreen(target))
	}

	@Suppress("Unused")
	@Subcommand("grant")
	@CommandCompletion("@achievements @players")
	@CommandPermission("ion.achievements.grant")
	fun onAchievementGrant(sender: CommandSender, achievement: Achievement, target: String) {
		val playerData = transaction { PlayerData.getByUsername(target) }

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist.", target)
			return
		}

		val player = Bukkit.getPlayer(playerData.id.value)

		if (player == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} must be online.", target)
			return
		}

		player.rewardAchievement(achievement)

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Gave achievement {0} to {1}.",
			achievement.name,
			target
		)
	}

	@Suppress("Unused")
	@Subcommand("revoke")
	@CommandCompletion("@achievements @players")
	@CommandPermission("ion.achievements.revoke")
	fun onAchievementRevoke(sender: CommandSender, achievement: Achievement, target: String) {
		val playerData = transaction { PlayerData.getByUsername(target) }

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist.", target)
			return
		}

		transaction { playerData.removeAchievement(achievement) }

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Took achievement {0} from {1}.", achievement.name, target)
	}
}