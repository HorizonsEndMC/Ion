package net.horizonsend.ion.proxy

import co.aikar.commands.BungeeCommandManager
import net.dv8tion.jda.api.JDA
import java.util.concurrent.TimeUnit
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.database.initializeDatabase
import net.horizonsend.ion.common.loadConfiguration
import net.md_5.bungee.api.plugin.Plugin

// Special Exception Wildcard Imports
import net.horizonsend.ion.proxy.commands.bungee.*
import net.horizonsend.ion.proxy.commands.discord.*
import net.horizonsend.ion.proxy.listeners.*

@Suppress("Unused")
class IonProxy : Plugin() {
	// Static accessors because we're evil
	companion object {
		lateinit var plugin: IonProxy

		val configuration get() = plugin.configuration
		val jda: JDA? get() = plugin.jda
	}
	init { plugin = this }

	val configuration: ProxyConfiguration = loadConfiguration(dataFolder, "proxy.conf")

	val jda = try {
		JDABuilder.createLight(configuration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setEnableShutdownHook(false)
			.build()
	}  catch (e: Exception) {
		slF4JLogger.warn("Failed to start JDA as it was unable to login to Discord!")
		null
	}

	override fun onEnable() {
		initializeDatabase(dataFolder)

		// Listener Registration
		val pluginManager = proxy.pluginManager

		pluginManager.registerListener(this, LoginListener())
		pluginManager.registerListener(this, ProxyPingListener(proxy, configuration))
		pluginManager.registerListener(this, VotifierListener(configuration))

		jda?.let {
			pluginManager.registerListener(this, PlayerDisconnectListener())
		}

		// Minecraft Command Registration
		val commandManager = BungeeCommandManager(this)

		commandManager.registerCommand(VoteCommand(configuration))
		commandManager.registerCommand(BungeeInfoCommand())

		jda?.let {
			commandManager.registerCommand(BungeeAccountCommand(jda, configuration))
		}

		// Java Discord API
		jda?.let {
			// Prune Inactive Members
			jda.getRoleById(configuration.unlinkedRole)?.let {
				jda.getGuildById(configuration.discordServer)?.prune(30, it)
			}

			// Discord Commands
			val jdaCommandManager = JDACommandManager(jda, configuration)

			jdaCommandManager.registerGuildCommand(DiscordAccountCommand(configuration))
			jdaCommandManager.registerGuildCommand(DiscordInfoCommand())
			jdaCommandManager.registerGuildCommand(PlayerListCommand(proxy))
			jdaCommandManager.registerGuildCommand(ResyncCommand(configuration))

			jdaCommandManager.build()

			// Live Player Count
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

	private fun removeOnlineRoleFromEveryone() = jda?.let {
		val guild = jda.getGuildById(configuration.discordServer) ?: return@let
		val role = guild.getRoleById(configuration.onlineRole) ?: return@let

		guild.getMembersWithRoles(role).forEach { member ->
			guild.removeRoleFromMember(member, role).queue()
		}
	}
}