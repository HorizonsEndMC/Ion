package net.horizonsend.ion.proxy.listeners.waterfall

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class ServerConnectListener : Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerConnectEvent(event: ServerConnectEvent) {
		val player = WrappedPlayer(event.player)
		PLUGIN.playerServerMap[event.player] = event.target

		if (event.reason == ServerConnectEvent.Reason.JOIN_PROXY) {
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
