package net.horizonsend.ion.discord

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.CommonConfig
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.discord.command.JDACommandManager
import net.horizonsend.ion.discord.command.discordCommands
import net.horizonsend.ion.discord.configuration.DiscordConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

object IonDiscordBot {
	private val logger: Logger = LoggerFactory.getLogger("WikipediaFeaturedBot")

	val dataFolder = getAppFolder()
	val configurationFolder = dataFolder.resolve("configuration")

	val configuration = Configuration.load<DiscordConfiguration>(configurationFolder, "discord.json")

	val discord = try {
		JDABuilder.createLight(configuration.discordBotToken)
			.setEnabledIntents(GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			.setChunkingFilter(ChunkingFilter.ALL)
			.disableCache(CacheFlag.values().toList())
			.setEnableShutdownHook(false)
			.build()
			.awaitReady()
	} catch (exception: Exception) {
		exit("Failed to start JDA, exiting", exception)
	}

	val server = discord.getGuildById(configuration.guildID) ?: exit("Could not get server", NullPointerException())

	val commandManager = JDACommandManager(discord, configuration)

	fun enable() {
		CommonConfig.init(configurationFolder)

		components.forEach(IonComponent::onEnable)

		for (command in discordCommands) commandManager.registerGuildCommand(command)
		commandManager.build()

		DBManager.INITIALIZATION_COMPLETE = true
	}

	private fun disable() {
		components.forEach(IonComponent::onDisable)

		discord.shutdown()
	}

	private fun getAppFolder(): File {
		return File(this::class.java.protectionDomain.codeSource.location.toURI()).parentFile
	}

	/** Safely shut down the process */
	fun exit(message: String, throwable: Throwable?): Nothing {
		if (throwable == null) logger.info("Exiting: $message") else logger.error("The discord bot has encountered an exception. Exiting. $throwable")

		disable()

		exitProcess(-1)
	}
}
