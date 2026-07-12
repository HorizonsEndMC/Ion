package net.horizonsend.ion.server.features.world.environment.weather.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.util.DurationConfig
import net.horizonsend.ion.server.configuration.util.DurationRange
import java.util.concurrent.TimeUnit

@Serializable
data class WorldWeatherConfiguration(
	val weatherSeparation: DurationRange = DurationRange(DurationConfig(TimeUnit.MINUTES, 15), DurationConfig(TimeUnit.MINUTES, 60)),
	val weatherTypes: List<WeatherTypeConfiguration> = listOf()
)
