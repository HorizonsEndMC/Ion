package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import java.net.URL
import net.horizonsend.ion.proxy.proxy

class ServerConnectedListener {
	private val resourcePackTag: String = URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
		.readText()
		.substringAfter("\",\"tag_name\":\"")
		.substringBefore("\",")

	@Suppress("Unused")
	@Subscribe(order = PostOrder.LAST)
	fun onServerConnectedEvent(event: ServerConnectedEvent): EventTask = EventTask.async {
		event.player.sendResourcePackOffer(
			proxy.createResourcePackBuilder(
				"https://github.com/HorizonsEndMC/ResourcePack/releases/download/${resourcePackTag}/HorizonsEndResourcePack.zip"
			).build()
		)
	}
}