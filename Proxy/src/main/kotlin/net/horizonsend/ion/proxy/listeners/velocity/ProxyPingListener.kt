package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.util.Favicon
import java.net.URL
import java.util.Base64
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.horizonsend.ion.proxy.proxy
import net.horizonsend.ion.proxy.proxyConfiguration
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

@VelocityListener
@Suppress("Unused")
class ProxyPingListener {
	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val icon =
		Favicon(
			"data:image/png;base64,${
				Base64.getEncoder().encodeToString(
					URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png").readBytes()
				)
			}"
		)

	@Subscribe(order = PostOrder.NORMAL)
	fun onProxyPingEvent(event: ProxyPingEvent): EventTask = EventTask.async {
		event.ping = ServerPing(
			ServerPing.Version(760, "1.19.1/2"),
			ServerPing.Players(proxy.playerCount, proxy.playerCount + 1, listOf()),
			miniMessage().deserialize("${proxyConfiguration.motdFirstLine}\n")
				.append(miniMessage().deserialize(messages.random())),
			icon
		)
	}
}