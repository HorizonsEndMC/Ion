package net.horizonsend.ion.proxy.listeners.bungee

import java.net.URL
import javax.imageio.ImageIO
import net.horizonsend.ion.proxy.ProxyConfiguration
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

@Suppress("Unused")
class ProxyPingListener(private val proxy: ProxyServer, private val configuration: ProxyConfiguration) : Listener {
	private val version = ServerPing.Protocol("1.19.2", 760)

	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val icon = Favicon.create(
		ImageIO.read(URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png"))
	)

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onProxyPingEvent(event: ProxyPingEvent) {
		event.response.version = version
		event.response.players = ServerPing.Players(
			proxy.onlineCount + 1,
			proxy.onlineCount,
			proxy.players.map { ServerPing.PlayerInfo(it.name, it.uniqueId) }.toTypedArray()
		)
		event.response.descriptionComponent = TextComponent(
			*TextComponent.fromLegacyText("${configuration.motdFirstLine}\n${messages.random()}")
		)
		event.response.setFavicon(icon)
	}
}