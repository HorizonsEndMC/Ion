package net.horizonsend.ion.server.features.world.environment.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.world.environment.weather.configuration.WorldWeatherConfiguration

@Serializable
data class WorldEnvironmentConfiguration(
	val atmosphericPressure: Double = 1.013,
	val moduleConfiguration: List<EnvironmentModuleConfiguration> = listOf(),
	val weatherConfiguration: WorldWeatherConfiguration = WorldWeatherConfiguration()
)
