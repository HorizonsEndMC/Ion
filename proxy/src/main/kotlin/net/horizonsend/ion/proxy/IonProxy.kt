package net.horizonsend.ion.proxy

import co.aikar.commands.BungeeCommandManager
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.proxy.commands.BungeeInfoCommand
import net.horizonsend.ion.proxy.commands.MessageCommand
import net.horizonsend.ion.proxy.commands.ReplyCommand
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.horizonsend.ion.proxy.wrappers.WrappedProxy
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.plugin.Plugin
import java.util.UUID

lateinit var PLUGIN: IonProxy private set

@Suppress("Unused")
class IonProxy : Plugin() {
	private val startTime = System.currentTimeMillis()

	init { PLUGIN = this }

	val adventure = BungeeAudiences.create(this)

	val configuration: ProxyConfiguration = Configuration.load(dataFolder, "proxy.json")

	val playerServerMap = mutableMapOf<UUID, ServerInfo>()

	val proxy = WrappedProxy(getProxy())

	init {
		prefixProvider = {
			when (it) {
				is WrappedProxy -> ""
				is WrappedPlayer -> "to ${it.name}: "
				else -> "to [Unknown]: "
			}
		}

		CommonConfig.init(dataFolder)

		proxy.pluginManager.apply {
			for (component in components) {
				if (component is IonProxyComponent) registerListener(this@IonProxy, component)

				component.onEnable()
			}
		}

		val commandManager = BungeeCommandManager(this).apply {
			registerCommand(BungeeInfoCommand())
			registerCommand(MessageCommand())
			registerCommand(ReplyCommand())
		}

		DBManager.INITIALIZATION_COMPLETE = true
	}

	private val endTime = System.currentTimeMillis()

	init { slF4JLogger.info("Loaded in %,3dms".format(endTime - startTime)) }

	override fun onDisable() {
		adventure.close()
	}
}

