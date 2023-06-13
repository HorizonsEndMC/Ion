package net.horizonsend.ion.server.features.achievements

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.screens.ScreenManager.openScreen
import net.starlegacy.database.schema.misc.SLPlayer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.pull

@CommandAlias("achievements")
@Suppress("Unused")
class AchievementsCommand : BaseCommand() {
	@Default
	fun onAchievementsList(sender: Player) {
		sender.openScreen(AchievementsScreen(sender.name))
	}

	@Default
	fun onAchievementsList(sender: Player, target: String) {
		sender.openScreen(AchievementsScreen(target))
	}

	@Subcommand("grant")
	@CommandCompletion("@achievements @players")
	@CommandPermission("ion.achievements.grant")
	fun onAchievementGrant(sender: CommandSender, achievement: Achievement, target: String) {
		val player = Bukkit.getPlayer(target) ?: return sender.userError("Player $target must be online.")

		player.rewardAchievement(achievement)

		sender.success("Gave achievement ${achievement.name} to $target.")
	}

	@Subcommand("revoke")
	@CommandCompletion("@achievements @players")
	@CommandPermission("ion.achievements.revoke")
	fun onAchievementRevoke(sender: CommandSender, achievement: Achievement, target: String) {
		val playerData = SLPlayer[target] ?: return sender.userError("Player $target does not exist.")

		SLPlayer.updateById(playerData._id, pull(SLPlayer::achievements, achievement))

		sender.success("Took achievement ${achievement.name} from $target.")
	}
}
