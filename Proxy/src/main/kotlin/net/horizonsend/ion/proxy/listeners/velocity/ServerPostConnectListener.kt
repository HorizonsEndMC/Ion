package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.ProxyServer
import java.net.URL
import java.security.MessageDigest
import net.horizonsend.ion.proxy.annotations.VelocityListener

@VelocityListener
@Suppress("Unused")
class ServerPostConnectListener(private val velocity: ProxyServer) {
	private val resourcePackOffer = kotlin.run {
		val tag = URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
			.readText()
			.substringAfter("\",\"tag_name\":\"")
			.substringBefore("\",")

		val url = "https://github.com/HorizonsEndMC/ResourcePack/releases/download/$tag/HorizonsEndResourcePack.zip"

		velocity.createResourcePackBuilder(url)
			.setHash(
				MessageDigest.getInstance("SHA-1")
					.digest(URL(url).readBytes())
			)
			.build()
	}

	@Subscribe(order = PostOrder.LAST)
	fun onServerConnectedEvent(event: ServerPostConnectEvent): EventTask = EventTask.async {
		if (event.previousServer == null) event.player.sendResourcePackOffer(resourcePackOffer)
	}
}