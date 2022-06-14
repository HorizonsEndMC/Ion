package net.horizonsend.ion.proxy.listeners

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.ProxyServer

class ServerPostConnectListener(val server: ProxyServer) {
	@Subscribe(order = PostOrder.LAST)
	@Suppress("Unused", "UnstableApiUsage")
	fun onServerPostConnectEvent(event: ServerPostConnectEvent): EventTask = EventTask.async {
		event.player.sendResourcePackOffer(
			server.createResourcePackBuilder(
				"https://github.com/HorizonsEndMC/ResourcePack/releases/download/Latest/HorizonsEndResourcePack.zip"
			).build()
		)
	}
}