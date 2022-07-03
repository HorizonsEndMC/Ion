package net.horizonsend.ion.proxy

import co.aikar.commands.VelocityCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.horizonsend.ion.proxy.commands.LinksCommand
import net.horizonsend.ion.proxy.listeners.PreLoginListener
import net.horizonsend.ion.proxy.listeners.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.ServerConnectedListener
import org.slf4j.Logger

@Suppress("Unused")
@Plugin(id = "ion", name = "Ion") // While we do not use this for generating velocity-plugin.json, ACF requires it.
class IonProxy @Inject constructor(
	private val proxy: ProxyServer,
	@Suppress("Unused_Parameter") slF4JLogger: Logger
) {
	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		proxy.eventManager.register(this, ServerConnectedListener(proxy))
		proxy.eventManager.register(this, ProxyPingListener(proxy))
		proxy.eventManager.register(this, PreLoginListener())

		VelocityCommandManager(proxy, this).apply {
			registerCommand(LinksCommand())
		}
	}
}