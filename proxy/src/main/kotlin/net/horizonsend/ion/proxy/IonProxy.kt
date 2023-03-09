package net.horizonsend.ion.proxy

import co.aikar.commands.BungeeCommandManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus.ONLINE
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.proxy.commands.discord.DiscordAccountCommand
import net.horizonsend.ion.proxy.commands.discord.DiscordInfoCommand
import net.horizonsend.ion.proxy.commands.discord.PlayerListCommand
import net.horizonsend.ion.proxy.commands.discord.ResyncCommand
import net.horizonsend.ion.proxy.commands.waterfall.BungeeAccountCommand
import net.horizonsend.ion.proxy.commands.waterfall.BungeeInfoCommand
import net.horizonsend.ion.proxy.commands.waterfall.MessageCommand
import net.horizonsend.ion.proxy.commands.waterfall.ReplyCommand
import net.horizonsend.ion.proxy.commands.waterfall.VoteCommand
import net.horizonsend.ion.proxy.listeners.waterfall.PlayerDisconnectListener
import net.horizonsend.ion.proxy.listeners.waterfall.ProxyPingListener
import net.horizonsend.ion.proxy.listeners.waterfall.ServerConnectListener
import net.horizonsend.ion.proxy.listeners.waterfall.VotifierListener
import net.horizonsend.ion.proxy.managers.ReminderManager
import net.horizonsend.ion.proxy.managers.SyncManager
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

lateinit var PLUGIN: IonProxy private set

@Suppress("Unused")
class IonProxy : Plugin() {
	val configuration: ProxyConfiguration
	val discord: JDA?

	val playerServerMap = mutableMapOf<ProxiedPlayer, ServerInfo>()

	init {
		try {
			slF4JLogger.info("Enabled in %,3dms".format(measureTimeMillis {
					PLUGIN = this

					configuration = Configuration.load(dataFolder, "proxy.json")

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
					commandManager.registerCommand(MessageCommand())
					commandManager.registerCommand(ReplyCommand())

					discord = try {
						JDABuilder.createLight(configuration.discordBotToken)
							.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
							.setMemberCachePolicy(MemberCachePolicy.ALL)
							.setChunkingFilter(ChunkingFilter.ALL)
							.disableCache(CacheFlag.values().toList())
							.setEnableShutdownHook(false)
							.build()
					} catch (exception: Exception) {
						slF4JLogger.warn("Failed to start JDA", exception)
						null
					}

					discord?.let {
						SyncManager(it, configuration).onEnable()

						// Commands
						commandManager.registerCommand(BungeeAccountCommand(discord, configuration))

						// Discord Commands
						val jdaCommandManager = JDACommandManager(discord, configuration)

						jdaCommandManager.registerGuildCommand(DiscordAccountCommand(configuration))
						jdaCommandManager.registerGuildCommand(DiscordInfoCommand())
						jdaCommandManager.registerGuildCommand(PlayerListCommand(proxy))
						jdaCommandManager.registerGuildCommand(ResyncCommand(discord, configuration))

						jdaCommandManager.build()

						// Live Player Count
						proxy.scheduler.schedule(this, {
							discord.presence.setPresence(ONLINE, playing("with ${proxy.onlineCount} players!"))
						}, 0, 5, TimeUnit.SECONDS)
					}
			}))
		} catch (exception: Exception) {
			proxy.stop()
			slF4JLogger.error("An exception occurred during plugin startup! The server will now exit.")
			throw exception
		}
	}

	override fun onDisable() {
		discord?.shutdown()

		Connectivity.close()
	}
}
