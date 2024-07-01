package net.horizonsend.ion.proxy.wrappers

import com.velocitypowered.api.plugin.PluginManager
import com.velocitypowered.api.proxy.ProxyServer
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience

class WrappedProxy(private val inner: ProxyServer) : ForwardingAudience.Single {
	private val audience = inner
	override fun audience(): Audience = audience

	val pluginManager: PluginManager get() = inner.pluginManager
	val scheduler: WrappedScheduler = WrappedScheduler(PLUGIN, inner.scheduler)
	val onlineCount get() = inner.playerCount
	val players get() = inner.allPlayers.map { WrappedPlayer(it) }
}
