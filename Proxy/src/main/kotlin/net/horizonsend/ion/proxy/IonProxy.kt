package net.horizonsend.ion.proxy

import co.aikar.commands.BaseCommand
import co.aikar.commands.BungeeCommandManager
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaType
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
import net.horizonsend.ion.proxy.annotations.GlobalCommand
import net.horizonsend.ion.proxy.annotations.GuildCommand
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import org.reflections.Reflections
import org.reflections.Store
import org.reflections.scanners.Scanners.SubTypes
import org.reflections.scanners.Scanners.TypesAnnotated
import org.reflections.util.QueryFunction

@Suppress("Unused")
class IonProxy : Plugin() {
	private val configuration: ProxyConfiguration = loadConfiguration(dataFolder)

	private val jda = try {
		JDABuilder.createLight(configuration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setEnableShutdownHook(false)
			.build()
	}  catch (_: LoginException) {
		slF4JLogger.warn("Failed to start JDA as it was unable to login to Discord!")
		null
	}

	override fun onEnable() {
		initializeCommon(dataFolder)

		val reflections = Reflections("net.horizonsend.ion.proxy")

		val commandManager = BungeeCommandManager(this)

		reflectionsRegister(reflections, SubTypes.of(Listener::class.java), "listeners") {
			proxy.pluginManager.registerListener(this, it as Listener)
		}

		reflectionsRegister(reflections, SubTypes.of(BaseCommand::class.java), "commands") {
			commandManager.registerCommand(it as BaseCommand)
		}

		jda?.let { jda ->
			val jdaCommandManager = JDACommandManager(jda, configuration)

			reflectionsRegister(reflections, TypesAnnotated.of(GlobalCommand::class.java), "global discord commands") {
				jdaCommandManager.registerGlobalCommand(it)
			}

			reflectionsRegister(reflections, TypesAnnotated.of(GuildCommand::class.java), "guild discord commands") {
				jdaCommandManager.registerGuildCommand(it)
			}

			jdaCommandManager.build()

			proxy.scheduler.schedule(this, {
				jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("with ${proxy.onlineCount} players!"))
			}, 0, 5, TimeUnit.SECONDS)
		}

		removeOnlineRoleFromEveryone()
	}

	override fun onDisable() {
		removeOnlineRoleFromEveryone()

		jda?.shutdown()
	}

	private fun <T> reflectionsRegister(
		reflections: Reflections,
		scanner: QueryFunction<Store, T>,
		name: String,
		execute: (Any) -> Unit
	) {
		reflections.get(scanner.asClass<T>())
			.map clazzMap@ { clazz ->
				val constructor = clazz.kotlin.constructors.first()

				constructor.javaConstructor?.newInstance(*constructor.parameters.map { when (it.type) {
					ProxyConfiguration::class.createType() -> configuration
					ProxyServer::class.createType() -> proxy
					IonProxy::class.createType() -> this
					JDA::class.createType(nullable = true) -> jda
					JDA::class.createType() -> if (jda != null) jda else {
						slF4JLogger.error("${clazz.name} has not been loaded as it requires JDA which is unavailable.")
						return@clazzMap null
					}
					else -> {
						slF4JLogger.error("Unable to provide ${it.type.javaType.typeName} to ${clazz.simpleName}.")
						return@clazzMap null
					}
				}}.toTypedArray())
			}
			.filterNotNull()
			.also { logger.info("Loading ${it.size} $name.") }
			.forEach(execute)
	}

	private fun removeOnlineRoleFromEveryone() = jda?.let {
		val guild = jda.getGuildById(configuration.discordServer) ?: return@let
		val role = guild.getRoleById(configuration.onlineRole) ?: return@let

		guild.getMembersWithRoles(role).forEach { member ->
			guild.removeRoleFromMember(member, role).queue()
		}
	}
}