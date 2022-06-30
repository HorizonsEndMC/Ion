package net.horizonsend.ion.proxy.listeners

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.util.Favicon
import java.net.URL
import java.util.Base64
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

class ProxyPingListener(private val server: ProxyServer) {
	private val version = ServerPing.Version(759, "1.19")

	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val motdLine1 =
		miniMessage().deserialize("<b><blue>Horizon's End</blue></b> / <grey>A space server with working ships.</grey>\n")

	private val icon =
		Favicon(
			"data:image/png;base64,${
				Base64.getEncoder().encodeToString(
					URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png").readBytes()		
				)
			}"
		)

	@Subscribe(order = PostOrder.NORMAL)
	@Suppress("unused")
	fun onProxyPingEvent(event: ProxyPingEvent): EventTask = EventTask.async {
		event.ping = ServerPing(
			version,
			ServerPing.Players(server.playerCount, server.playerCount + 1, listOf()),
			motdLine1.append(miniMessage().deserialize(messages.random())),
			icon
		)
	}
}