package net.horizonsend.ion.proxy.listeners

import net.horizonsend.ion.proxy.IonProxy
import java.net.URL
import javax.imageio.ImageIO
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class ProxyPingListener : Listener {
	private val protocol = ServerPing.Protocol("1.19.2", 760)

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
			IonProxy.proxy.onlineCount + 1,
			IonProxy.proxy.onlineCount,
			IonProxy.proxy.players.map { ServerPing.PlayerInfo(it.name, it.uniqueId) }.toTypedArray()
		)
		descriptionComponent = TextComponent(
			*TextComponent.fromLegacyText("${IonProxy.configuration.motdFirstLine}\n${messages.random()}")
		)
		setFavicon(icon)
	}
}