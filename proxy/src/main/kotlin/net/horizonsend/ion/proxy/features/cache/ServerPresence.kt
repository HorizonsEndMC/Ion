package net.horizonsend.ion.proxy.features.cache

import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.md_5.bungee.api.Favicon
import net.md_5.bungee.api.ServerPing
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.URL
import javax.imageio.ImageIO

object ServerPresence : IonProxyComponent() {
	val primaryVersion = 762
	val primaryVersionName = "1.19.4"
	val allowedVersions = intArrayOf(759, 760, 761, 762)

	private val messages =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }

	private val icon = Favicon.create(ImageIO.read(URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png")))

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onProxyPingEvent(event: ProxyPingEvent) = event.response.run {
		val clientVersion = event.connection.version
		version = ServerPing.Protocol(primaryVersionName, if (allowedVersions.contains(clientVersion)) clientVersion else primaryVersion)
		players = ServerPing.Players(
			PLUGIN.proxy.onlineCount + 1,
			PLUGIN.proxy.onlineCount,
			PLUGIN.proxy.players.map { ServerPing.PlayerInfo(it.name, it.uniqueId) }.toTypedArray()
		)
		descriptionComponent = TextComponent(
			*TextComponent.fromLegacyText("${PLUGIN.configuration.motdFirstLine}\n${messages.random()}")
		)
		setFavicon(icon)
	}
}
