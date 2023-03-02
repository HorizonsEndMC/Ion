package net.horizonsend.ion.server

import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused", "UnstableApiUsage")
class IonBootstrapper : PluginBootstrap {
	override fun bootstrap(context: PluginProviderContext) {}
	override fun createPlugin(context: PluginProviderContext): JavaPlugin = IonServer
}
