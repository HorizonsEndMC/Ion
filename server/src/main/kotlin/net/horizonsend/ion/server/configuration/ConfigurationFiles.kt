package net.horizonsend.ion.server.configuration

import net.horizonsend.ion.common.utils.discord.DiscordConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.miscellaneous.LegacyConfig
import java.io.File

object ConfigurationFiles {
	val configurationFolder = IonServer.configurationFolder
	val sharedDataFolder by lazy { File(legacySettings.get().sharedFolder).apply { mkdirs() } }

	private val configurationFiles = mutableListOf<ConfigurationFile<*>>()

	fun reload() = configurationFiles.forEach { it.reload() }

	val legacySettings = defineConfigurationFile<LegacyConfig>(configurationFolder, "legacyConfiguration")

	val featureFlags = defineConfigurationFile<FeatureFlags>(configurationFolder, "features")

	val serverConfiguration = defineConfigurationFile<ServerConfiguration>(configurationFolder, "server")

	val starshipBalancing = defineConfigurationFile<StarshipTypeBalancing>(configurationFolder, "starshipbalancing")

	val globalGassesConfiguration = defineConfigurationFile<GlobalGassesConfiguration>(configurationFolder, "gasses")

	val tradeConfiguration = defineConfigurationFile<TradeConfiguration>(configurationFolder, "trade")

	val aiSpawningConfiguration = defineConfigurationFile<AISpawningConfiguration>(configurationFolder, "aiSpawning")

	val discordSettings = defineConfigurationFile<DiscordConfiguration>(configurationFolder, "discord")



	inline fun <reified T: Any> defineConfigurationFile(directory: File, fileName: String): ConfigurationFile<T> {
		val new = ConfigurationFile(T::class, directory, fileName)
		return new
	}

	fun ensureSharedDataFolder(): ConfigurationFiles {
		//TODO

		return this
	}
}
