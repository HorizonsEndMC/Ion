package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.command.SLCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.util.multimapOf
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ListCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("list|who")
	fun execute(sender: CommandSender) {
		val players: Collection<Player> = Bukkit.getOnlinePlayers()

		if (players.isEmpty()) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "No players online")
			return
		}

		val nationMap = multimapOf<Oid<Nation>?, Player>()

		for (player in players) {
			val playerNation: Oid<Nation>? = PlayerCache[player].nation
			nationMap[playerNation].add(player)
		}

		val nationIdsSortedByName: List<Oid<Nation>?> = nationMap.keySet()
			.sortedBy { id -> id?.let { NationCache[it].name } ?: "_" }

		for (nationId: Oid<Nation>? in nationIdsSortedByName) {
			val members: Collection<Player> = nationMap[nationId].sortedBy { SLXP[it] }

			val nationText = nationId?.let { "<purple>${NationCache[it].name}" } ?: "<yellow><italic>Nationless"

			sender.sendRichMessage(
				"$nationText <dark_purple>:(<light_purple>${members.count()}<dark_gray>):<gray> ${
				members.joinToString { player ->
// 					val nationPrefix = PlayerCache[player].nationTag?.let { "<reset>$it " } ?: ""
					return@joinToString "<gray>[<aqua>${Levels[player]}<gray>] " +
// 							"$nationPrefix" +
						"<gray>${player.name}"
				}
				}"
			)
		}
	}
}
