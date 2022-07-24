package net.horizonsend.ion.proxy

import co.aikar.commands.VelocityCommandManager
import com.google.inject.Inject
import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.managers.CommonManager
import net.horizonsend.ion.common.utilities.loadConfiguration
import net.horizonsend.ion.proxy.commands.discord.DiscordAccountCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordInfoCommand
import net.horizonsend.ion.proxy.commands.discord.PlayerListCommand
import net.horizonsend.ion.proxy.commands.discord.ResyncCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityAccountCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityInfoCommand
import net.horizonsend.ion.proxy.listeners.velocity.DisconnectListener
import net.horizonsend.ion.proxy.listeners.velocity.LoginListener
import net.horizonsend.ion.proxy.listeners.velocity.PreLoginListener
import net.horizonsend.ion.proxy.listeners.velocity.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.velocity.ServerConnectedListener
import org.slf4j.Logger

internal lateinit var proxy: ProxyServer private set
internal lateinit var logger: Logger private set
internal lateinit var dataDirectory: Path private set
internal lateinit var proxyConfiguration: ProxyConfiguration private set
internal lateinit var jda: JDA private set

@Suppress("Unused")
@Plugin(id = "ion", name = "Ion") // While we do not use this for generating velocity-plugin.json, ACF requires it.
class IonProxy @Inject constructor(proxy0: ProxyServer, logger0: Logger, @DataDirectory dataDirectory0: Path) {
	init {
		proxy = proxy0
		logger = logger0
		dataDirectory = dataDirectory0
		proxyConfiguration = loadConfiguration(dataDirectory)
		jda = JDABuilder.createLight(proxyConfiguration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setActivity(Activity.playing("horizonsend.net"))
			.setEnableShutdownHook(false)
			.build()
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		CommonManager.init(dataDirectory)

		arrayOf(
			LoginListener(), PreLoginListener(), ProxyPingListener(), ServerConnectedListener(), DisconnectListener()
		).forEach {
			proxy.eventManager.register(this, it)
		}

		VelocityCommandManager(proxy, this).apply {
			registerCommand(VelocityInfoCommand())
			registerCommand(VelocityAccountCommand())
		}

		JDACommandManager(
			jda,
			DiscordInfoCommand(),
			DiscordAccountCommand(),
			PlayerListCommand(),
			ResyncCommand()
		)

		removeOnlineRoleFromEveryone()
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyShutdownEvent(event: ProxyShutdownEvent): EventTask = EventTask.async {
		removeOnlineRoleFromEveryone()

		jda.shutdown()
	}

	private fun removeOnlineRoleFromEveryone() {
		val guild = jda.getGuildById(proxyConfiguration.discordServer) ?: return
		val role = guild.getRoleById(proxyConfiguration.onlineRole) ?: return

		guild.getMembersWithRoles(role).forEach { member ->
			guild.removeRoleFromMember(member, role).queue()
		}
	}
}