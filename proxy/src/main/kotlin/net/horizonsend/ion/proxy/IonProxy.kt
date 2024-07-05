package net.horizonsend.ion.proxy

import co.aikar.commands.VelocityCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.common.utils.configuration.CommonConfig
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.discord.DiscordConfiguration
import net.horizonsend.ion.proxy.commands.bungee.BungeeInfoCommand
import net.horizonsend.ion.proxy.commands.bungee.MessageCommand
import net.horizonsend.ion.proxy.commands.bungee.ReplyCommand
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.horizonsend.ion.proxy.wrappers.WrappedProxy
import java.nio.file.Path
import java.util.logging.Logger

lateinit var PLUGIN: IonProxy private set

@Suppress("Unused")
@Plugin(
	id = "ion",
	name = "IonProxy",
)
class IonProxy @Inject constructor(val server: ProxyServer, val logger: Logger, @DataDirectory val dataDirectory: Path) {
	private val startTime = System.currentTimeMillis()

	init { PLUGIN = this }

	val configuration: ProxyConfiguration = Configuration.load(dataFolder, "proxy.json")
	val discordConfiguration: DiscordConfiguration = Configuration.load(dataFolder, "discord.json")

	val dataFolder get() = dataDirectory.toFile()

	val proxy = WrappedProxy(server)
	lateinit var commandManager: VelocityCommandManager

	init {
		prefixProvider = {
			when (it) {
				is WrappedProxy -> ""
				is WrappedPlayer -> "to ${it.name}: "
				else -> "to [Unknown]: "
			}
		}
	}

	@Subscribe
	fun onProxyInitialization(event: ProxyInitializeEvent?) {
		CommonConfig.init(dataFolder)

		val eventManager = server.eventManager

		for (component in components) {
			if (component is IonProxyComponent) eventManager.register(this@IonProxy, component)

			component.onEnable()
		}

		commandManager = VelocityCommandManager(this.server, this).apply {
			registerCommand(BungeeInfoCommand())
			registerCommand(MessageCommand)
			registerCommand(ReplyCommand())
		}

		DBManager.INITIALIZATION_COMPLETE = true

		val endTime = System.currentTimeMillis()
		logger.info("Loaded in %,3dms".format(endTime - startTime))
	}

	@Subscribe
	fun onProxyClose(event: ProxyShutdownEvent) {
		for (component in components.asReversed()) try {
			component.onDisable()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}

