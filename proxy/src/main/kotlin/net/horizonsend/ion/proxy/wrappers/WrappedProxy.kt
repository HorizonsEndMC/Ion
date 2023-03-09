package net.horizonsend.ion.proxy.wrappers

import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.PluginManager
import net.md_5.bungee.api.scheduler.TaskScheduler

class WrappedProxy(private val inner: ProxyServer) : ForwardingAudience.Single {
	private val audience = PLUGIN.adventure.all()
	override fun audience(): Audience = audience

	val pluginManager: PluginManager get() = inner.pluginManager
	val scheduler: TaskScheduler get() = inner.scheduler
	val onlineCount get() = inner.onlineCount
	val players get() = inner.players.map { WrappedPlayer(it) }
}
