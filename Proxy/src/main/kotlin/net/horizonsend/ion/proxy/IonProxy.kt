package net.horizonsend.ion.proxy

// Special Exception Wildcard Imports
import net.horizonsend.ion.proxy.commands.bungee.*
import net.horizonsend.ion.proxy.commands.discord.*
import net.horizonsend.ion.proxy.listeners.*
import net.horizonsend.ion.proxy.managers.ReminderManager

import co.aikar.commands.BungeeCommandManager
import java.util.concurrent.TimeUnit
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.loadConfiguration
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin

@Suppress("Unused")
class IonProxy : Plugin() {
	init { Ion = this }

	companion object {
		@JvmStatic lateinit var Ion: IonProxy private set
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
	} catch (e: Exception) {
		slF4JLogger.warn("Failed to start JDA as it was unable to login to Discord!")
		null
	}

	val playerServerMap = mutableMapOf<ProxiedPlayer, ServerInfo>()

	override fun onEnable() { try {
		Connectivity.open(dataFolder)

		// Schedule Reminders
		ReminderManager.scheduleReminders()

		// Listener Registration
		val pluginManager = proxy.pluginManager

		pluginManager.registerListener(this, PlayerDisconnectListener())
		pluginManager.registerListener(this, ProxyPingListener())
		pluginManager.registerListener(this, ServerConnectListener())
		try { pluginManager.registerListener(this, VotifierListener()) } catch (_: NoClassDefFoundError) {}

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
		}
	} catch (exception: Exception) {
		slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.", exception)
		proxy.stop()
	}}

	override fun onDisable() {
		jda?.shutdown()

		Connectivity.close()
	}
}