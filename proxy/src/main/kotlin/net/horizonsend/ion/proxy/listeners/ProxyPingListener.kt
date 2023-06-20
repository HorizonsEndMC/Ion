package net.horizonsend.ion.proxy.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.util.Favicon
import net.horizonsend.ion.proxy.IonProxy
import net.kyori.adventure.text.minimessage.MiniMessage
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class ProxyPingListener {
	private val primaryVersion = 762
	private val primaryVersionName = "1.19.4"
	private val allowedVersions = intArrayOf(759, 760, 761, 762)

	private var messages = generateMessages()
	private val icon =
		Favicon.create(ImageIO.read(URL("https://github.com/HorizonsEndMC/ResourcePack/raw/main/pack.png")))

	@Subscribe
	fun onProxyPingEvent(event: ProxyPingEvent) {
		event.ping =
			ServerPing.builder().run {
				val clientVersion = event.connection.protocolVersion.protocol
				version(
					ServerPing.Version(
						if (allowedVersions.contains(clientVersion)) clientVersion else primaryVersion,
						primaryVersionName
					)
				)

				onlinePlayers(
					IonProxy.proxy.playerCount
				)

				maximumPlayers(
					IonProxy.proxy.playerCount + 1
				)

				samplePlayers(
					*IonProxy.proxy.allPlayers.map { ServerPing.SamplePlayer(it.username, it.uniqueId) }.toTypedArray()
				)

				description(
					MiniMessage.miniMessage().deserialize(
						"${IonProxy.configuration.motdFirstLine}\n${messages.random()}"
					)
				)

				favicon(
					icon
				)
			}.build()
	}

	fun startTask(): ProxyPingListener {
		IonProxy.proxy.scheduler.buildTask(IonProxy) {
			messages = generateMessages()
		}.repeat(5, TimeUnit.MINUTES)

		return this
	}

	private fun generateMessages() =
		URL("https://raw.githubusercontent.com/HorizonsEndMC/MOTDs/main/MOTD")
			.readText()
			.split('\n')
			.filterNot { it.isEmpty() }
}
