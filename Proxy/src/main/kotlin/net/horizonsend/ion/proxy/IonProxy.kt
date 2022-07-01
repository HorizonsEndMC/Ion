package net.horizonsend.ion.proxy

import co.aikar.commands.VelocityCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.proxy.ProxyServer
import net.horizonsend.ion.proxy.commands.LinksCommand
import net.horizonsend.ion.proxy.listeners.PreLoginListener
import net.horizonsend.ion.proxy.listeners.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.ServerConnectedListener
import org.slf4j.Logger

@Suppress("Unused")
class IonProxy @Inject constructor(
	private val server: ProxyServer,
	@Suppress("Unused_Parameter") slF4JLogger: Logger
) {
	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		server.eventManager.register(this, ServerConnectedListener(server))
		server.eventManager.register(this, ProxyPingListener(server))
		server.eventManager.register(this, PreLoginListener())

		VelocityCommandManager(server, this).apply {
			registerCommand(LinksCommand())
		}
	}
}