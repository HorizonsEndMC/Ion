package net.horizonsend.ion.proxy.listeners

import net.horizonsend.ion.proxy.IonProxy.Companion.Ion
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.URL
import javax.imageio.ImageIO

class ProxyPingListener : Listener {
	private val protocol = ServerPing.Protocol("1.19.3", 761)

	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val icon = Favicon.create(ImageIO.read(URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png")))

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onProxyPingEvent(event: ProxyPingEvent) = event.response.run {
		version = protocol
		players = ServerPing.Players(
			Ion.proxy.onlineCount + 1,
			Ion.proxy.onlineCount,
			Ion.proxy.players.map { ServerPing.PlayerInfo(it.name, it.uniqueId) }.toTypedArray()
		)
		descriptionComponent = TextComponent(
			*TextComponent.fromLegacyText("${Ion.configuration.motdFirstLine}\n${messages.random()}")
		)
		setFavicon(icon)
	}
}
