package net.horizonsend.ion.proxy

import co.aikar.commands.BungeeCommandManager
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus.ONLINE
import net.dv8tion.jda.api.entities.Activity.playing
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.Configuration
import net.horizonsend.ion.common.Connectivity
import net.horizonsend.ion.common.extensions.prefixProvider
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
import net.horizonsend.ion.proxy.wrappers.WrappedPlayer
import net.horizonsend.ion.proxy.wrappers.WrappedProxy
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.TimeUnit

lateinit var PLUGIN: IonProxy private set

@Suppress("Unused")
class IonProxy : Plugin() {
	private val startTime = System.currentTimeMillis()

	init { PLUGIN = this }

	val adventure = BungeeAudiences.create(this)

	val configuration: ProxyConfiguration = Configuration.load(dataFolder, "proxy.json")

	val discord = try {
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

	val playerServerMap = mutableMapOf<ProxiedPlayer, ServerInfo>()

	val proxy = WrappedProxy(getProxy())

	init {
		Connectivity.open(dataFolder)

		prefixProvider = {
			when (it) {
				is WrappedProxy -> ""
				is WrappedPlayer -> "to ${it.name}: "
				else -> "to [Unknown]: "
			}
		}

		ReminderManager.scheduleReminders()

		proxy.pluginManager.apply {
			registerListener(this@IonProxy, PlayerDisconnectListener())
			registerListener(this@IonProxy, ProxyPingListener())
			registerListener(this@IonProxy, ServerConnectListener())
			try { registerListener(this@IonProxy, VotifierListener()) } catch (_: NoClassDefFoundError) {}
		}

		val commandManager = BungeeCommandManager(this).apply {
			registerCommand(VoteCommand(configuration))
			registerCommand(BungeeInfoCommand())
			registerCommand(MessageCommand())
			registerCommand(ReplyCommand())
		}

		discord?.let {
			commandManager.registerCommand(BungeeAccountCommand(discord, configuration))

			JDACommandManager(discord, configuration).apply {
				registerGuildCommand(DiscordAccountCommand(configuration))
				registerGuildCommand(DiscordInfoCommand())
				registerGuildCommand(PlayerListCommand(getProxy()))
				registerGuildCommand(ResyncCommand(discord, configuration))

				build()
			}

			proxy.scheduler.schedule(this, {
				discord.presence.setPresence(ONLINE, playing("with ${proxy.onlineCount} players!"))
			}, 0, 5, TimeUnit.SECONDS)
		}
	}

	private val endTime = System.currentTimeMillis()

	init { slF4JLogger.info("Loaded in %,3dms".format(endTime - startTime)) }

	override fun onEnable() {
		discord?.let { SyncManager(it, configuration).onEnable() }
	}

	override fun onDisable() {
		discord?.shutdown()

		Connectivity.close()
	}
}
