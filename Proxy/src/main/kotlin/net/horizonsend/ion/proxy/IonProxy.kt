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
import javax.security.auth.login.LoginException
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.initializeCommon
import net.horizonsend.ion.common.utilities.loadConfiguration
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.horizonsend.ion.proxy.commands.discord.DiscordAccountCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordInfoCommand
import net.horizonsend.ion.proxy.commands.discord.PlayerListCommand
import net.horizonsend.ion.proxy.commands.discord.ResyncCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityAccountCommand
import net.horizonsend.ion.proxy.commands.velocity.VelocityInfoCommand
import org.reflections.Reflections
import org.reflections.scanners.Scanners.TypesAnnotated
import org.slf4j.Logger

internal lateinit var proxy: ProxyServer private set
internal lateinit var proxyConfiguration: ProxyConfiguration private set
internal lateinit var jda: JDA private set

@Suppress("Unused")
@Plugin(id = "ion", name = "Ion") // While we do not use this for generating velocity-plugin.json, ACF requires it.
class IonProxy @Inject constructor(proxy0: ProxyServer, val logger: Logger, @DataDirectory val dataDirectory: Path) {
	init {
		proxy = proxy0
		proxyConfiguration = loadConfiguration(dataDirectory)
		try {
			jda = JDABuilder.createLight(proxyConfiguration.discordBotToken)
				.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.setChunkingFilter(ChunkingFilter.ALL)
				.disableCache(CacheFlag.values().toList())
				.setEnableShutdownHook(false)
				.build()
		} catch (_: LoginException) {
			logger.warn("Failed to start JDA as it was unable to login to Discord!")
		}
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		initializeCommon(dataDirectory)

		val reflections = Reflections("net.horizonsend.ion.proxy")

		reflections.get(TypesAnnotated.of(VelocityListener::class.java).asClass<Any>())
			.map { it.constructors[0] }
			.map { it.newInstance() }
			.also { logger.info("Loading ${it.size} listeners.") }
			.forEach { proxy.eventManager.register(this, it) }

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

		Thread {
			while (true) {
				jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("horizonsend.net"))
				Thread.sleep(5000)
				jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("Minecraft 1.19.1/2"))
				Thread.sleep(5000)
				jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("with ${proxy.playerCount} players!"))
				Thread.sleep(5000)
			}
		}.start()
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