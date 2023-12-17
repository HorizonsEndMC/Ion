package net.horizonsend.ion.proxy.features.messaging

import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.features.ConnectionMessages
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.util.UUID

object PlayerTracking : IonProxyComponent() {
	val playerServerMap = mutableMapOf<UUID, ServerInfo>()

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerConnectEvent(event: ServerConnectEvent) {
		if (event.isCancelled) return
		val alreadyConnected = playerServerMap[event.player.uniqueId]

		playerServerMap[event.player.uniqueId] = event.target

		if (alreadyConnected == null) {
			ConnectionMessages.onLogin(event.player, event.target)
		} else {
			ConnectionMessages.onSwitchServer(event.player, event.target)
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerDisconnectEvent(event: PlayerDisconnectEvent) {
		val serverInfo = playerServerMap.remove(event.player.uniqueId) ?: return

		ConnectionMessages.onPlayerDisconnect(event.player, serverInfo)
	}
}
