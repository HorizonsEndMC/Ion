package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.util.Favicon
import java.net.URL
import java.util.Base64
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

@VelocityListener
@Suppress("Unused")
class ProxyPingListener(private val velocity: ProxyServer, private val configuration: ProxyConfiguration) {
	private val version = ServerPing.Version(760, "1.19.1/2")

	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val icon = kotlin.run {
		val image = URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png").readBytes()
		Favicon("data:image/png;base64,${Base64.getEncoder().encodeToString(image)}")
	}

	@Subscribe(order = PostOrder.NORMAL)
	fun onProxyPingEvent(event: ProxyPingEvent): EventTask = EventTask.async {
		event.ping = ServerPing(
			version,
			ServerPing.Players(
				velocity.playerCount,
				velocity.playerCount + 1,
				velocity.allPlayers.map {ServerPing.SamplePlayer(it.username, it.uniqueId) }
			),
			miniMessage().deserialize("${configuration.motdFirstLine}\n")
				.append(miniMessage().deserialize(messages.random())),
			icon
		)
	}
}