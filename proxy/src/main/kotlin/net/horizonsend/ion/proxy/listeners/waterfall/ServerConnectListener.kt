package net.horizonsend.ion.proxy.listeners.waterfall

import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.Colors
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.special
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ServerConnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerConnectEvent(event: ServerConnectEvent): Unit = transaction {
		val player = WrappedPlayer(event.player)
		PLUGIN.playerServerMap[event.player] = event.target

		if (event.reason == ServerConnectEvent.Reason.JOIN_PROXY) {
			var isNew = false

			val playerData = PlayerData[player.uniqueId] ?: PlayerData.new(player.uniqueId) { isNew = true }
			playerData.username = player.name

			if (isNew) {
				PLUGIN.proxy.special("Welcome <white>${player.name}</white> to the server!")

				PLUGIN.discord?.let { jda ->
					val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

					globalChannel.sendMessageEmbeds(
						messageEmbed(
							description = "Welcome ${player.name.replace("_", "\\_")} to the server!",
							color = Colors.SPECIAL
						)
					).queue()
				}
			} else {
				PLUGIN.proxy.information("<dark_gray>[<green>+ <gray>${event.target.name}<dark_gray>] <white>${event.player.name}")

				PLUGIN.discord?.let { jda ->
					val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

					globalChannel.sendMessageEmbeds(
						messageEmbed(
							description = "[+ ${event.target.name}] ${player.name.replace("_", "\\_")}",
							color = ChatColor.GREEN.color.rgb
						)
					).queue()
				}

				val promptToVote = playerData.voteTimes.find { it.dateTime.isBefore(LocalDateTime.now().minusDays(1)) } != null
				if (promptToVote) player.special("Hey ${player.name}! Remember to vote for the server to help us grow the Horizon's End community!")
			}
		} else {
			PLUGIN.proxy.information("<dark_gray>[<blue>> <gray>${event.target.name}<dark_gray>] <white>${player.name}")

			PLUGIN.discord?.let { jda ->
				val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

				globalChannel.sendMessageEmbeds(
					messageEmbed(
						description = "[> ${event.target.name}] ${player.name.replace("_", "\\_")}",
						color = ChatColor.BLUE.color.rgb
					)
				).queue()
			}
		}
	}
}
