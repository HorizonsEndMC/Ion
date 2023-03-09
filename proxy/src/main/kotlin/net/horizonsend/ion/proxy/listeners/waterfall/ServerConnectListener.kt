package net.horizonsend.ion.proxy.listeners.waterfall

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ServerConnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerConnectEvent(event: ServerConnectEvent) {
		val playerAudience = PLUGIN.adventure.player(event.player)
		PLUGIN.playerServerMap[event.player] = event.target

		if (event.reason == ServerConnectEvent.Reason.JOIN_PROXY) {
			var isNew = false

			val playerData = PlayerData[event.player.uniqueId]
				?.update { username = event.player.name }
				?: PlayerData.new(event.player.uniqueId) {
					username = event.player.name
					isNew = true
				}

			if (isNew) {
				PLUGIN.adventure.all().information("<gold>Welcome <white>${event.player.name}</white> to the server!")

				PLUGIN.discord?.let { jda ->
					val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

					globalChannel.sendMessageEmbeds(
						messageEmbed(
							description = "Welcome ${event.player.name.replace("_", "\\_")} to the server!",
							color = ChatColor.GOLD.color.rgb
						)
					).queue()
				}
			} else {
				PLUGIN.adventure.all().information("<dark_gray>[<green>+ <gray>${event.target.name}<dark_gray>] <white>${event.player.name}")

				PLUGIN.discord?.let { jda ->
					val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

					globalChannel.sendMessageEmbeds(
						messageEmbed(
							description = "[+ ${event.target.name}] ${event.player.name.replace("_", "\\_")}",
							color = ChatColor.GREEN.color.rgb
						)
					).queue()
				}

				val promptToVote = transaction {
					playerData.voteTimes.find { it.dateTime.isBefore(LocalDateTime.now().minusDays(1)) } != null
				}

				if (promptToVote) playerAudience.information(
					"Hey ${event.player.name}! Remember to vote for the server to help us grow the Horizon's End community!"
				)
			}
		} else {
			PLUGIN.adventure.all().information("<dark_gray>[<blue>> <gray>${event.target.name}<dark_gray>] <white>${event.player.name}")

			PLUGIN.discord?.let { jda ->
				val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

				globalChannel.sendMessageEmbeds(
					messageEmbed(
						description = "[> ${event.target.name}] ${event.player.name.replace("_", "\\_")}",
						color = ChatColor.BLUE.color.rgb
					)
				).queue()
			}
		}
	}
}
