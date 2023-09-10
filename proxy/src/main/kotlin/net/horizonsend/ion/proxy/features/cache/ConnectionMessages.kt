package net.horizonsend.ion.proxy.features.cache

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.messageEmbed
import net.horizonsend.ion.proxy.utils.isBanned
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

object ConnectionMessages : IonProxyComponent() {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onLogin(event: ServerConnectedEvent) { // This event is only called when logging into the server the first time
		val info = event.server.info

		PLUGIN.proxy.information("<dark_gray>[<green>+ <gray>${info.name}<dark_gray>] <white>${event.player.name}")

		PLUGIN.discord?.let { jda ->
			val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

			globalChannel.sendMessageEmbeds(
				messageEmbed(
					description = "[+ ${info.name}] ${event.player.name.replace("_", "\\_")}",
					color = ChatColor.GREEN.color.rgb
				)
			).queue()
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onSwitchServer(event: ServerConnectEvent) {
		if (event.isCancelled) return
		if (event.reason == ServerConnectEvent.Reason.JOIN_PROXY) return

		val player = WrappedPlayer(event.player)
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

	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
		val serverName = PLUGIN.playerServerMap.remove(event.player.uniqueId)!!.name

		if (event.player.isBanned()) return

		PLUGIN.proxy.information("<dark_gray>[<red>- <gray>$serverName<dark_gray>] <white>${event.player.displayName}")

		PLUGIN.discord?.let { jda ->
			val globalChannel = jda.getTextChannelById(PLUGIN.configuration.globalChannel) ?: return@let

			globalChannel.sendMessageEmbeds(
				messageEmbed(
					description = "[- $serverName] ${event.player.name.replace("_", "\\_")}",
					color = ChatColor.RED.color.rgb
				)
			).queue()
		}
	}
}
