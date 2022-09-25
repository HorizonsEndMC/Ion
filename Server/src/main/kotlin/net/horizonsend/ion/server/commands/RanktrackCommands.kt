package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.utilities.calculateRank
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("ranktrack|rt")
@Suppress("unused")
class RanktrackCommands : BaseCommand() {

	@Subcommand("info|i")
	@CommandCompletion("@players")
	@CommandPermission("ion.ranktracks.info")
	fun onRanktrackinfo(sender: CommandSender, target: String) {
		val playerData = PlayerData[target]
		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Playerdata for {0} doesn't exist", target)
			return
		}
		sender.sendRichMessage(
			"""
				<yellow>Player: <yellow><gold>${target}<gold><reset>
				<blue>Ranktrack Xp: <blue><aqua>${playerData.xp}<aqua>
				<dark_gray>Ranktrack Type: <dark_gray><yellow>${playerData.ranktracktype.displayName}<yellow>
				<gray><red>Current Ranktrack<red>: <dark_red>${calculateRank(playerData).displayName}<dark_red>
			""".trimIndent()
		)
	}

	@Subcommand("addxp")
	@CommandCompletion("@players")
	@CommandPermission("ion.ranktracks.admin.addxp")
	fun onRanktrackXpGive(sender: CommandSender, target: String, xp: Int) {
		val playerData = PlayerData[target]

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist", target)
			return
		}

		val oldRank = calculateRank(playerData)

		playerData.update {
			playerData.xp =+ xp
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Added {0}xp to {1}", xp, target)

		val newrank = calculateRank(playerData)

		val player = Bukkit.getPlayer(playerData.minecraftUUID)
		if (newrank != oldRank) {
			player!!.sendFeedbackMessage(
				FeedbackType.ALERT,
				"Your xp has been altered by {0} by {1}, your rank is now {2}",
				xp,
				sender.name,
				newrank.displayName
			)
		}
		else player!!.sendFeedbackMessage(
			FeedbackType.ALERT,
			"Your xp has been altered by {0} by {1}",
			xp,
			sender.name
		)
	}

	@Subcommand("setTrack")
	@CommandCompletion("@players")
	@CommandPermission("ion.ranktracks.admin.settrack")
	fun onSetRankTrack(sender: CommandSender, target: String, ranktrack: Ranktrack){
		val playerData = PlayerData[target]

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist", target)
			return
		}
		val currentRankTrack = playerData.ranktracktype

		playerData.update {
			playerData.ranktracktype = ranktrack
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Set {0}'s Ranktrack from {1} to {2}", target,currentRankTrack, playerData.ranktracktype )
	}

	@Subcommand("reset")
	@CommandCompletion("@players")
	@CommandPermission("ion.ranktracks.admin.reset")
	fun onRanktrackReset(sender: CommandSender, target: String) {
		val playerData = PlayerData[target]

		if (playerData == null) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Player {0} does not exist", target)
			return
		}

		playerData.update {
			playerData.xp = 0
			playerData.ranktracktype = Ranktrack.REFUGEE
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Reset {0} to 0xp and to Refugee", target)

		val player = Bukkit.getPlayer(playerData.minecraftUUID)
		player!!.sendFeedbackMessage(
			FeedbackType.ALERT,
			"Your ranktrack and xp have both been reset by {0}",
			sender.name
		)
	}
}