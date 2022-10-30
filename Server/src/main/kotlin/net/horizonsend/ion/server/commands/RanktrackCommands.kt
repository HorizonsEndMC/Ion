package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.managers.ScreenManager.openScreen
import net.horizonsend.ion.server.screens.RanktrackScreen
import net.horizonsend.ion.server.utilities.addRanktrackXP
import net.horizonsend.ion.server.utilities.calculateRank
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.schema.misc.SLPlayer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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

	@Subcommand("migrate")
	@CommandPermission("ion.ranktracks.migrate")
	fun onRanktrackMigrate(sender: CommandSender){
		val playerCache = PlayerCache[(sender as? Player)!!]
		val slPlayer: SLPlayer = SLPlayer[playerCache.id] ?: return
		if (slPlayer.hasMigrated){
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "We're not stupid enough not to stop you migrating twice")
			return
		}
		val totalXp = (50*(slPlayer.level)* max(1.0, floor(slPlayer.level/50.0)) +50)
		var xpToGive = 0
		if ((0 <= totalXp) && (totalXp >= 20000)){
			xpToGive = totalXp.toInt()
		}
		else if (totalXp>20000){
			xpToGive = sqrt(totalXp).pow((totalXp.div(8.0).pow(-1))).toInt() - 333553
		}
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "You have been migrated your old xp to ranktrack xp")
		sender.addRanktrackXP(xpToGive)
		slPlayer.hasMigrated = true
	}

	@Subcommand("gui")
	@CommandPermission("ion.ranktracks.gui")
	fun onRanktrackGui(sender: CommandSender){
		(sender as? Player)!!.openScreen(RanktrackScreen(sender.name))
	}
}