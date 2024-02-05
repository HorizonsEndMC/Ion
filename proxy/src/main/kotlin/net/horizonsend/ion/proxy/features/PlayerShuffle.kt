package net.horizonsend.ion.proxy.features

import net.horizonsend.ion.proxy.IonProxyComponent
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.event.EventHandler
import java.util.UUID

/**
 * Means of sending a player to a server and running a callback upon connection
 **/
object PlayerShuffle : IonProxyComponent() {
	private val tasks = mutableMapOf<UUID, WaitTask>()

	fun send(player: ProxiedPlayer, destination: ServerInfo, callback: (ProxiedPlayer) -> Unit) {
		if (player.server == destination) {
			callback(player)
			return
		}

		tasks[player.uniqueId] = WaitTask(callback)
		player.connect(destination)
	}

	@EventHandler
	fun onPlayerConnect(event: ServerConnectedEvent) {
		val task = tasks[event.player.uniqueId] ?: return

		task.callback(event.player)
	}

	class WaitTask(val callback: (ProxiedPlayer) -> Unit)
}
