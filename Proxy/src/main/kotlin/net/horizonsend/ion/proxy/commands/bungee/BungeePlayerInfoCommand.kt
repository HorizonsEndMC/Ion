package net.horizonsend.ion.proxy.commands.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.proxy.calculateRanktrack
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("playerinfo|pi|pinfo|testingpinfo")
@Suppress("unused")
class BungeePlayerInfoCommand : BaseCommand() {
	@Default
	@CommandCompletion("@players")
	fun onPlayerInfo(sender: ProxiedPlayer, target: String) {
		val playerData = PlayerData[target]
		if (playerData == null) {
			sender.sendMessage(
				*ComponentBuilder().append(
					ComponentBuilder("Player: $target, does not exist").color(
						ChatColor.RED
					).create()
				).create()
			)
			return
		}
		sender.sendMessage(
			*ComponentBuilder()
				.append(
					ComponentBuilder("Player: $target\n")
						.color(ChatColor.GOLD)
						.underlined(true)
						.create()
				)
				.append(
					ComponentBuilder("XP: ${playerData.xp}\n")
						.color(ChatColor.AQUA)
						.underlined(false)
						.create()
				)
				.append(
					ComponentBuilder("Ranktrack: ${playerData.ranktracktype.displayName}\n")
						.color(ChatColor.BLUE)
						.underlined(false)
						.create()
				)
				.append(
					ComponentBuilder("Rank: ${calculateRanktrack(playerData).displayName}\n")
						.color(ChatColor.RED)
						.underlined(false)
						.create()
				)
				.append(
				ComponentBuilder("LastLogoffTime: ${if (playerData.lastLoggofftime.isNotEmpty())("${playerData.lastLoggofftime.toList().first().second} on(${playerData.lastLoggofftime.toList().first().first})") else "Player is Online"}")
					.color(ChatColor.GRAY)
					.underlined(false)
					.create()
				)
			.create()
		)
	}
}