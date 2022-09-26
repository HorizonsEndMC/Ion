package net.horizonsend.ion.proxy

import co.aikar.commands.BungeeCommandManager
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
import net.horizonsend.ion.proxy.commands.bungee.BungeeAccountCommand
import net.horizonsend.ion.proxy.commands.bungee.BungeeInfoCommand
import net.horizonsend.ion.proxy.commands.bungee.VoteCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordAccountCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordInfoCommand
import net.horizonsend.ion.proxy.commands.discord.PlayerListCommand
import net.horizonsend.ion.proxy.commands.discord.ResyncCommand
import net.horizonsend.ion.proxy.listeners.bungee.LoginListener
import net.horizonsend.ion.proxy.listeners.bungee.PlayerDisconnectListener
import net.horizonsend.ion.proxy.listeners.bungee.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.bungee.VoteListener
import net.md_5.bungee.api.plugin.Plugin

@Suppress("Unused")
class IonProxy : Plugin() {
	private val configuration: ProxyConfiguration = loadConfiguration(dataFolder, "proxy.conf")

	private val jda = try {
		JDABuilder.createLight(configuration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setEnableShutdownHook(false)
			.build()
	}  catch (_: Exception) {
		slF4JLogger.warn("Failed to start JDA as it was unable to login to Discord!")
		null
	}

	override fun onEnable() {
		initializeDatabase(dataFolder)

		// Listener Registration
		val pluginManager = proxy.pluginManager

		pluginManager.registerListener(this, ProxyPingListener(proxy, configuration))

		jda?.let {
			pluginManager.registerListener(this, LoginListener(configuration, jda))
			pluginManager.registerListener(this, PlayerDisconnectListener(jda, configuration))
			pluginManager.registerListener(this, VoteListener(configuration))
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