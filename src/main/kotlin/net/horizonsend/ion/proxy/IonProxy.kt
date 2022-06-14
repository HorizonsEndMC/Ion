package net.horizonsend.ion.proxy

import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.EventTask.async
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.horizonsend.ion.proxy.listeners.ServerPostConnectListener
import org.slf4j.Logger

@Plugin(
	id = "ion",
	name = "Ion",
	version = "unspecified",
	authors = ["Peter Crawley"]
)
@Suppress("Unused")
class IonProxy @Inject constructor(
	private val server: ProxyServer,
	@Suppress("Unused_Parameter") slF4JLogger: Logger
) {
	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = async {
		server.eventManager.register(this, ServerPostConnectListener(server))
	}
}