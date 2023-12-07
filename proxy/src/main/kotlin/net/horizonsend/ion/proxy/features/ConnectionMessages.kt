package net.horizonsend.ion.proxy.features

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.messaging.ProxyDiscordMessaging
import net.horizonsend.ion.proxy.utils.isBanned
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

object ConnectionMessages : IonProxyComponent() {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onLogin(event: ServerConnectedEvent) { // This event is only called when logging into the server the first time
		val info = event.server.info

		if (event.player.isBanned()) return
		if (PLUGIN.playerServerMap.containsKey(event.player.uniqueId)) return

		PLUGIN.proxy.information("<dark_gray>[<green>+ <gray>${info.name}<dark_gray>] <white>${event.player.name}")

		ProxyDiscordMessaging.globalEmbed(Embed(
			description = "[+ ${info.name}] ${event.player.name.replace("_", "\\_")}",
			color = NamedTextColor.GREEN.value()
		))
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onSwitchServer(event: ServerConnectEvent) {
		if (event.isCancelled) return
		if (event.reason == ServerConnectEvent.Reason.JOIN_PROXY) return

		val player = WrappedPlayer(event.player)
		PLUGIN.proxy.information("<dark_gray>[<blue>> <gray>${event.target.name}<dark_gray>] <white>${player.name}")

		ProxyDiscordMessaging.globalEmbed(Embed(
			description = "[> ${event.target.name}] ${player.name.replace("_", "\\_")}",
			color = NamedTextColor.BLUE.value()
		))
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
		val serverName = PLUGIN.playerServerMap.remove(event.player.uniqueId)!!.name

		if (event.player.isBanned()) return
		if (PLUGIN.playerServerMap.containsKey(event.player.uniqueId)) return

		PLUGIN.proxy.information("<dark_gray>[<red>- <gray>$serverName<dark_gray>] <white>${event.player.displayName}")

		ProxyDiscordMessaging.globalEmbed(Embed(
			description = "[- $serverName] ${event.player.name.replace("_", "\\_")}",
			color = NamedTextColor.RED.value()
		))
	}
}
