package net.horizonsend.ion.proxy

import co.aikar.commands.VelocityCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.horizonsend.ion.common.managers.CommonManager
import net.horizonsend.ion.common.utilities.loadConfiguration
import net.horizonsend.ion.proxy.commands.discord.DiscordInfoCommand
import net.horizonsend.ion.proxy.commands.discord.PlayerListCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityInfoCommand
import net.horizonsend.ion.proxy.listeners.velocity.LoginListener
import net.horizonsend.ion.proxy.listeners.velocity.PreLoginListener
import net.horizonsend.ion.proxy.listeners.velocity.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.velocity.ServerConnectedListener
import org.slf4j.Logger

@Suppress("Unused")
@Plugin(id = "ion", name = "Ion") // While we do not use this for generating velocity-plugin.json, ACF requires it.
class IonProxy @Inject constructor(
	private val proxy: ProxyServer,
	@Suppress("Unused_Parameter") slF4JLogger: Logger,
	@DataDirectory private val dataDirectory: Path
) {
	val proxyConfiguration = loadConfiguration<ProxyConfiguration>(dataDirectory)

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		CommonManager.init(dataDirectory)

		arrayOf(
			LoginListener(this),
			PreLoginListener(),
			ProxyPingListener(proxy),
			ServerConnectedListener(proxy)
		).forEach {
			proxy.eventManager.register(this, it)
		}

		VelocityCommandManager(proxy, this).apply {
			registerCommand(VelocityInfoCommand())
		}

		JDACommandManager(
			JDABuilder.createLight(proxyConfiguration.discordBotToken)
				.setActivity(Activity.playing("horizonsend.net"))
				.build(),
			DiscordInfoCommand(),
			PlayerListCommand(proxy)
		)
	}
}