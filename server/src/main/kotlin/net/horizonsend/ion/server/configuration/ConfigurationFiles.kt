package net.horizonsend.ion.server.configuration

import net.horizonsend.ion.common.utils.discord.DiscordConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.configuration.AIEmities
import net.horizonsend.ion.server.features.ai.configuration.AIPowerModes
import net.horizonsend.ion.server.features.ai.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.ai.configuration.steering.AIContextConfiguration
import net.horizonsend.ion.server.features.ai.configuration.steering.AISteeringConfiguration
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.TransportConfiguration
import net.horizonsend.ion.server.miscellaneous.LegacyConfig
import java.io.File

object ConfigurationFiles {
	val configurationFolder = IonServer.dataFolder.resolve("configuration").apply { mkdirs() }
	val sharedDataFolder by lazy { File(legacySettings.get().sharedFolder).apply { mkdirs() } }

	private val configurationFiles = mutableListOf<ConfigurationFile<*>>()

	val legacySettings = defineConfigurationFile<LegacyConfig>(configurationFolder, "legacyConfiguration")

	val featureFlags = defineConfigurationFile<FeatureFlags>(configurationFolder, "features")

	val serverConfiguration = defineConfigurationFile<ServerConfiguration>(configurationFolder, "server")

	val starshipBalancing = defineConfigurationFile<StarshipTypeBalancing>(configurationFolder, "starshipbalancing")

	val pvpBalancing = defineConfigurationFile<PVPBalancingConfiguration>(configurationFolder, "pvpBalancing")

	val globalGassesConfiguration = defineConfigurationFile<GlobalGassesConfiguration>(configurationFolder, "gasses")

	val tradeConfiguration = defineConfigurationFile<TradeConfiguration>(configurationFolder, "trade")

	val aiSpawningConfiguration = defineConfigurationFile<AISpawningConfiguration>(configurationFolder, "aiSpawning")

	val aiSteeringConfiguration = defineConfigurationFile<AISteeringConfiguration>(configurationFolder, "aiSteering")

	val aiEmityConfiguration = defineConfigurationFile<AIEmities>(configurationFolder, "aiEmities")

	val aiPowerModeConfiguration = defineConfigurationFile<AIPowerModes>(configurationFolder,"aiPowerModes")

	val aiContextConfiguration = defineConfigurationFile<AIContextConfiguration>(configurationFolder, "aiContexts")

	val discordSettings = defineConfigurationFile<DiscordConfiguration>(configurationFolder, "discord")

	val nationConfiguration = defineConfigurationFile<NationsConfiguration>(configurationFolder, "nation")

	val transportSettings = defineConfigurationFile<TransportConfiguration>(configurationFolder, "transport") { NewTransport.reload() }

	private inline fun <reified T: Any> defineConfigurationFile(directory: File, fileName: String, noinline callback: () -> Unit = {}): ConfigurationFile<T> {
		val new = ConfigurationFile(T::class, directory, fileName, callback)
		configurationFiles.add(new)

		return new
	}

	fun reload() = configurationFiles.forEach { it.reload() }
	fun saveToDisk() = configurationFiles.forEach { it.saveToDisk() }
}
