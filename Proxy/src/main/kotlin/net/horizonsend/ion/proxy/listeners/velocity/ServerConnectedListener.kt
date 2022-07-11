package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.horizonsend.ion.proxy.managers.ResourcePackDownloadManager
import net.horizonsend.ion.proxy.proxy

class ServerConnectedListener {
	@Suppress("Unused")
	@Subscribe(order = PostOrder.LAST)
	fun onServerConnectedEvent(event: ServerConnectedEvent): EventTask = EventTask.async {
		event.player.sendResourcePackOffer(
			proxy.createResourcePackBuilder(
				"https://github.com/HorizonsEndMC/ResourcePack/releases/download/${ResourcePackDownloadManager.resourcePackTag}/HorizonsEndResourcePack.zip"
			).build()
		)
	}
}