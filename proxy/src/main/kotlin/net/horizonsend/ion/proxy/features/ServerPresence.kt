package net.horizonsend.ion.proxy.features

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.proxy.server.ServerPing.SamplePlayer
import com.velocitypowered.api.util.Favicon
import net.horizonsend.ion.proxy.IonProxyComponent
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.net.URL
import java.util.EnumSet
import javax.imageio.ImageIO

object ServerPresence : IonProxyComponent() {
	private val primaryVersion = ProtocolVersion.MINECRAFT_1_20_3
	private val supportedProtocol = EnumSet.of(
		ProtocolVersion.MINECRAFT_1_19,
		ProtocolVersion.MINECRAFT_1_19_1,
		ProtocolVersion.MINECRAFT_1_19_3,
		ProtocolVersion.MINECRAFT_1_19_4,
		ProtocolVersion.MINECRAFT_1_20,
		ProtocolVersion.MINECRAFT_1_20_2,
		ProtocolVersion.MINECRAFT_1_20_3,
		ProtocolVersion.MINECRAFT_1_20_5,
		ProtocolVersion.MINECRAFT_1_21,
	)

	private val messages = URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
		.readText()
		.split('\n')
		.filterNot { it.isEmpty() }

	private val icon = Favicon.create(ImageIO.read(URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png")))

	@Subscribe
	fun onProxyPingEvent(event: ProxyPingEvent) {
		val response = event.ping.asBuilder()
		val clientVersion = event.connection.protocolVersion

		if (clientVersion.isSupported) {
			response.version(ServerPing.Version(clientVersion.protocol, clientVersion.name))
		} else {
			response.version(ServerPing.Version(primaryVersion.protocol, primaryVersion.name))
		}

		response.description(LegacyComponentSerializer.legacyAmpersand().deserialize("${PLUGIN.configuration.motdFirstLine}\n${messages.random()}"))
		response.favicon(icon)

		response.onlinePlayers(PLUGIN.proxy.onlineCount)
		response.maximumPlayers(PLUGIN.proxy.onlineCount + 1)
		response.samplePlayers(
			*PLUGIN.proxy.players.map { SamplePlayer(it.name, it.uniqueId) }.take(10).toTypedArray()
		)

		event.ping = response.build()
	}
}
