package net.horizonsend.ion.proxy

import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.EventTask.async
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import net.horizonsend.ion.common.configuration.ConfigurationProvider
import net.horizonsend.ion.proxy.listeners.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.ServerConnectedListener
import org.slf4j.Logger

@Suppress("Unused")
class IonProxy @Inject constructor(
	private val server: ProxyServer,
	@Suppress("Unused_Parameter") slF4JLogger: Logger,
	@DataDirectory val pluginDataDirectory: Path
) {
	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = async {
		server.eventManager.register(this, ServerConnectedListener(server))
		server.eventManager.register(this, ProxyPingListener(server))

		ConfigurationProvider.loadConfiguration(pluginDataDirectory)
	}
}