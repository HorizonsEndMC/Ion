package net.horizonsend.ion.proxy.features

import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

object ServerMapping : IonProxyComponent() {
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onServerConnectEvent(event: ServerConnectEvent) {
		PLUGIN.playerServerMap[event.player.uniqueId] = event.target
	}
}
