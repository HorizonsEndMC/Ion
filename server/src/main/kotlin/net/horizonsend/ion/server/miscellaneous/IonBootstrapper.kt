package net.horizonsend.ion.server.miscellaneous

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import net.horizonsend.ion.server.IonServer
import org.bukkit.plugin.java.JavaPlugin

@Suppress("Unused", "UnstableApiUsage")
class IonBootstrapper : PluginBootstrap {
	override fun bootstrap(context: BootstrapContext) {}
	override fun createPlugin(context: PluginProviderContext): JavaPlugin = IonServer
}
