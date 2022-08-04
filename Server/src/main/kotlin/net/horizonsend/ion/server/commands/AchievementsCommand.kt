package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.horizonsend.ion.common.utilities.feedback.sendFeedbackMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

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
	fun onAchievementGrant(sender: CommandSender, achievementString: String, target: String) {
		val achievement = try {
			Achievement.valueOf(achievementString)
		} catch (_: IllegalArgumentException) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Achievement {0} does not exist.", achievementString)
			return
		}

		val playerData = transaction { PlayerData.getByUsername(target) }

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist.", target)
			return
		}

		transaction { playerData.addAchievement(achievement) }

		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Gave achievement {0} to {1}. Please note, rewards are not given automatically.",
			achievement.name,
			target
		)
	}

	@Suppress("Unused")
	@Subcommand("revoke")
	@CommandCompletion("@players")
	@CommandPermission("ion.achievements.revoke")
	fun onAchievementRevoke(sender: CommandSender, achievementString: String, target: String) {
		val achievement = try {
			Achievement.valueOf(achievementString)
		} catch (_: IllegalArgumentException) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Achievement {0} does not exist.", achievementString)
			return
		}

		val playerData = transaction { PlayerData.getByUsername(target) }

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist.", target)
			return
		}

		transaction { playerData.removeAchievement(achievement) }

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Took achievement {0} from {1}.", achievement.name, target)
	}
}