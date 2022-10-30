package net.horizonsend.ion.proxy.commands.discord

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Name
import java.util.concurrent.TimeUnit
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.proxy.calculateRanktrack
import net.horizonsend.ion.proxy.messageEmbed
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ServerPing.PlayerInfo

@Suppress("Unused")
@CommandAlias("playerinfo")
@Description("Find some key info on a player")
class PlayerInfoCommand(private val proxy: ProxyServer) {
	@Default
	fun onPlayerInfo(event: SlashCommandInteractionEvent, @Name("player") @Description("player in question") player: String){
		val playerData = PlayerData[player]
		if (playerData == null){
			event.replyEmbeds(
				messageEmbed(
					description = "$player does not exist in our database.",
					color = 0xff8844
				)
			)
			return
		}
		event.replyEmbeds(
			messageEmbed(
				description =
				"""
					|Player: $player
					|RanktrackType: ${playerData.ranktracktype.displayName}
					|XP: ${playerData.xp}
					|Rank: ${calculateRanktrack(playerData)}
					|LastLogOffTime: ${if(playerData.lastLoggofftime.isNotEmpty()){playerData.lastLoggofftime.toList().first().first} else("Player is Online")}
				""".trimMargin(),
			)
		)
	}
}