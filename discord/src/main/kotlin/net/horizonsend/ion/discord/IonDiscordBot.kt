package net.horizonsend.ion.discord

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.database.DBManager
import net.horizonsend.ion.common.utils.Configuration
import net.horizonsend.ion.discord.configuration.DiscordConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

object IonDiscordBot {
	private val logger: Logger = LoggerFactory.getLogger("WikipediaFeaturedBot")
	lateinit var thread: Thread

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

	fun enable() {
		println("Hello, world!")

		components.forEach(IonComponent::onEnable)

		val server = discord.getGuildById(configuration.guildID)!!
		val channel = server.getTextChannelById(configuration.globalChannelId)!!
		channel.sendMessage("REEE").queue()

		DBManager.INITIALIZATION_COMPLETE = true
	}

	private fun getAppFolder(): File {
		return File(this::class.java.protectionDomain.codeSource.location.toURI()).parentFile
	}

	fun exit(message: String, throwable: Throwable?): Nothing {
		if (throwable == null) logger.info("Exiting: $message") else logger.error("The discord bot has encountered an exception. Exiting. $throwable")

		exitProcess(-1)
	}
}
