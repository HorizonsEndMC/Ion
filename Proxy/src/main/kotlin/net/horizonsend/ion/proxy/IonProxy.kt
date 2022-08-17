package net.horizonsend.ion.proxy

import co.aikar.commands.BaseCommand
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
import java.util.concurrent.TimeUnit
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
import org.reflections.Reflections
import org.reflections.scanners.Scanners.SubTypes
import org.reflections.scanners.Scanners.TypesAnnotated
import org.slf4j.Logger

@Deprecated("Use dependency injection.") internal lateinit var proxy: ProxyServer private set
@Deprecated("Use dependency injection.") internal lateinit var proxyConfiguration: ProxyConfiguration private set
@Deprecated("Use dependency injection.") internal lateinit var jda: JDA private set

@Suppress("Unused")
@Plugin(id = "ion", name = "Ion") // While we do not use this for generating velocity-plugin.json, ACF requires it.
class IonProxy @Inject constructor(
	val velocity: ProxyServer,
	val logger: Logger,
	@DataDirectory
	val dataDirectory: Path
) {
	val configuration: ProxyConfiguration = loadConfiguration(dataDirectory)

	val jda = try {
		JDABuilder.createLight(configuration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setEnableShutdownHook(false)
			.build()
	}  catch (_: LoginException) {
		logger.warn("Failed to start JDA as it was unable to login to Discord!")
		null
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyInitializeEvent(event: ProxyInitializeEvent): EventTask = EventTask.async {
		@Suppress("Deprecation") // Older code compatibility
		proxy = velocity

		@Suppress("Deprecation") // Older code compatibility
		proxyConfiguration = configuration

		@Suppress("Deprecation") // Older code compatibility
		if (jda != null) net.horizonsend.ion.proxy.jda = jda

		initializeCommon(dataDirectory)

		val reflections = Reflections("net.horizonsend.ion.proxy")

		reflections.get(TypesAnnotated.of(VelocityListener::class.java).asClass<Any>())
			.map { it.constructors[0] }
			.map { it.newInstance() }
			.also { logger.info("Loading ${it.size} listeners.") }
			.forEach { velocity.eventManager.register(this, it) }

		val commandManager = VelocityCommandManager(velocity, this)

		reflections.get(SubTypes.of(BaseCommand::class.java).asClass<Any>())
			.map { it.constructors[0] }
			.map { it.newInstance() }
			.also { logger.info("Loading ${it.size} commands.") }
			.forEach { commandManager.registerCommand(it as BaseCommand) }

		jda?.let {
			JDACommandManager(it, DiscordInfoCommand(), DiscordAccountCommand(), PlayerListCommand(), ResyncCommand())

			velocity.scheduler.buildTask(this, Runnable {
				it.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("with ${velocity.playerCount} players!"))
			}).repeat(5, TimeUnit.SECONDS).schedule()
		}

		removeOnlineRoleFromEveryone()
	}

	@Suppress("Unused_Parameter")
	@Subscribe(order = PostOrder.LAST)
	fun onProxyShutdownEvent(event: ProxyShutdownEvent): EventTask = EventTask.async {
		removeOnlineRoleFromEveryone()

		jda?.shutdown()
	}

	private fun removeOnlineRoleFromEveryone() = jda?.let {
		val guild = jda.getGuildById(configuration.discordServer) ?: return@let
		val role = guild.getRoleById(configuration.onlineRole) ?: return@let

		guild.getMembersWithRoles(role).forEach { member ->
			guild.removeRoleFromMember(member, role).queue()
		}
	}
}