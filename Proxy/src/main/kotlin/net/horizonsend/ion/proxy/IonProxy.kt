package net.horizonsend.ion.proxy

// Special Exception Wildcard Imports
import net.horizonsend.ion.proxy.commands.bungee.*
import net.horizonsend.ion.proxy.commands.discord.*
import net.horizonsend.ion.proxy.listeners.*

import co.aikar.commands.BungeeCommandManager
import java.util.concurrent.TimeUnit
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.database.closeDatabase
import net.horizonsend.ion.common.database.openDatabase
import net.horizonsend.ion.common.loadConfiguration
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.ForkJoinPool

@Suppress("Unused")
class IonProxy : Plugin() {
	init { Ion = this }

	companion object {
		@JvmStatic lateinit var Ion: IonProxy private set
	}

	private val openDatabaseFuture = ForkJoinPool.commonPool().submit {
		openDatabase(dataFolder)
	}

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

	val playerServerMap = mutableMapOf<ProxiedPlayer, ServerInfo>()

	override fun onEnable() {
		openDatabaseFuture.join()

		// Listener Registration
		val pluginManager = proxy.pluginManager

		pluginManager.registerListener(this, PlayerDisconnectListener())
		pluginManager.registerListener(this, ProxyPingListener())
		pluginManager.registerListener(this, ServerConnectListener())
		pluginManager.registerListener(this, VotifierListener())

		// Minecraft Command Registration
		val commandManager = BungeeCommandManager(this)

		commandManager.registerCommand(VoteCommand(configuration))
		commandManager.registerCommand(BungeeInfoCommand())

		// Discord
		jda?.let {
			// Commands
			commandManager.registerCommand(BungeeAccountCommand(jda, configuration))

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

			removeOnlineRoleFromEveryone()
		}
	}

	override fun onDisable() {
		closeDatabase()

		jda?.run {
			removeOnlineRoleFromEveryone()
			shutdown()
		}
	}

	private fun removeOnlineRoleFromEveryone() {
		val guild = jda!!.getGuildById(configuration.discordServer) ?: return
		val role = guild.getRoleById(configuration.onlineRole) ?: return

		guild.getMembersWithRoles(role).forEach { member ->
			guild.removeRoleFromMember(member, role).queue()
		}
	}
}