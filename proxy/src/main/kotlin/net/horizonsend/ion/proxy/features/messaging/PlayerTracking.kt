package net.horizonsend.ion.proxy.features.messaging

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.server.ServerInfo
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.features.ConnectionMessages
import java.util.UUID

object PlayerTracking : IonProxyComponent() {
	val playerServerMap = mutableMapOf<UUID, ServerInfo>()

	@Subscribe(order = PostOrder.LAST)
	fun onServerConnectEvent(event: ServerConnectedEvent) {
		playerServerMap[event.player.uniqueId] = event.server.serverInfo

		val previousServer = event.previousServer

		if (previousServer.isEmpty) {
			ConnectionMessages.onLogin(event.player, event.server.serverInfo)
		} else {
			ConnectionMessages.onSwitchServer(event.player, event.server.serverInfo)
		}
	}

	@Subscribe(order = PostOrder.LAST)
	fun onServerDisconnectEvent(event: DisconnectEvent) {
		val serverInfo = playerServerMap.remove(event.player.uniqueId) ?: return

		ConnectionMessages.onPlayerDisconnect(event.player, serverInfo)
	}
}
