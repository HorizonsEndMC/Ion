package net.horizonsend.ion.proxy

import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.extensions.prefixProvider
import net.horizonsend.ion.proxy.commands.proxy.VelocityInfoCommand
import net.horizonsend.ion.proxy.commands.proxy.VelocityMessageCommand
import net.horizonsend.ion.proxy.commands.proxy.VelocityReplyCommand
import net.horizonsend.ion.proxy.listeners.PlayerListeners
import net.horizonsend.ion.proxy.listeners.ProxyPingListener
import net.horizonsend.ion.proxy.managers.ReminderManager
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

val IonProxy = IonProxyPlugin.INSTANCE

@Plugin(
	id = "ion", name = "Ion", version = "0.1.0-SNAPSHOT",
	url = "https://horizonsend.net", description = "Ion", authors = ["Rattlyy", "Astralchroma", "Gutin"],
)
class IonProxyPlugin @Inject constructor(
	val proxy: ProxyServer,
	val logger: Logger,
	val event: EventManager,
	@DataDirectory val folder: Path
) {
	val dataFolder: File = folder.toFile()
	val configuration: ProxyConfiguration = Configuration.load(dataFolder, "proxy.json")

	@Subscribe
	fun onInit(e: ProxyInitializeEvent) {
		INSTANCE = this

		prefixProvider = {
			when (it) {
				is ProxyServer -> ""
				is Player -> "to ${it.gameProfile.name}: "
				else -> "to [Unknown]: "
			}
		}

		val commandManager = proxy.commandManager

		val infoCommand = commandManager.metaBuilder("info")
			.aliases("map", "wiki", "patreon", "rules")
			.plugin(this)
			.build()

		commandManager.register(infoCommand, VelocityInfoCommand())

		val messageCommand = commandManager.metaBuilder("message")
			.aliases("msg", "tell", "whisper", "w")
			.plugin(this)
			.build()


		commandManager.register(messageCommand, VelocityMessageCommand())

		val replyCommand = commandManager.metaBuilder("reply")
			.aliases("r")
			.plugin(this)
			.build()

		commandManager.register(replyCommand, VelocityReplyCommand())

		ReminderManager.scheduleReminders()

		event.apply {
			register(this@IonProxyPlugin, PlayerListeners())
			register(this@IonProxyPlugin, ProxyPingListener())
			register(this@IonProxyPlugin, VelocityMessageCommand)
		}

		if (configuration.discordEnabled) {
			discord()
		}
	}

	@Subscribe
	fun onDisable(e: ProxyShutdownEvent) {  }

	companion object {
		lateinit var INSTANCE: IonProxyPlugin
	}
}
